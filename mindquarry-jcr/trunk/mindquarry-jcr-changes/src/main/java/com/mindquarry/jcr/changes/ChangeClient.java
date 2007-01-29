package com.mindquarry.jcr.changes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.jcr.LoginException;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.rmi.client.ClientRepositoryFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ChangeClient {
    private static final String O_REPO = "r"; //$NON-NLS-1$

    private static final String O_USER = "u"; //$NON-NLS-1$

    private static final String O_PWD = "p"; //$NON-NLS-1$

    private static final String O_XSLT = "x"; //$NON-NLS-1$

    private static final String O_DRY = "d"; //$NON-NLS-1$

    private static Log log;

    /**
     * The options object for the command line applications.
     */
    private static final Options options;

    // static initialization of command line options
    static {
        Option repo = new Option(O_REPO, "repository", true, //$NON-NLS-1$
                "The endpoint where the repository is available.");
        repo.setRequired(true);

        Option user = new Option(O_USER, "user", true, //$NON-NLS-1$
                "The user name to be used for login to the repository.");
        user.setRequired(true);

        Option pwd = new Option(O_PWD, "password", true, //$NON-NLS-1$
                "The password to be used for login to the repository.");
        pwd.setRequired(true);

        Option xslt = new Option(O_XSLT, "xslt", true, //$NON-NLS-1$
                "The XSLT stylesheet to be used for making the changes.");
        xslt.setRequired(true);

        Option debug = new Option(
                O_DRY,
                "dry", false, //$NON-NLS-1$
                "Indicates if changes should be done as a dry run. "
                        + "In this mode no modifications to the repository are done. "
                        + "All changes are stored in local files for debugging purposes.");

        options = new Options();
        options.addOption(repo);
        options.addOption(user);
        options.addOption(pwd);
        options.addOption(xslt);
        options.addOption(debug);
    }

    public static void main(String[] args) {
        log = LogFactory.getLog(ChangeClient.class);
        log.info("Starting persistence client...");

        // parse command line arguments
        CommandLine line = null;
        CommandLineParser parser = new GnuParser();
        try {
            // parse the command line arguments
            line = parser.parse(options, args);
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Parsing failed. Reason: "
                    + exp.getLocalizedMessage());
            printUsage();
            return;
        }
        // check debug option
        boolean debug = false;
        if (line.hasOption(O_DRY)) {
            log.info("Change client runs in 'dry' mode.");
            debug = true;
        }
        // start change client
        ChangeClient cl = new ChangeClient();
        try {
            cl.run(line.getOptionValue(O_REPO), line.getOptionValue(O_USER), line
                    .getOptionValue(O_PWD), line.getOptionValue(O_XSLT), debug);
        } catch (Exception e) {
            log.error("Error while applying changes.", e);
        }
        log.info("Changes applied.");
    }

    private void run(String repoLocation, String login, String pwd,
            String xslt, boolean debug) throws Exception {
        Session session = login(repoLocation, login, pwd);

        ByteArrayOutputStream bos = exportRepositoryContent(session);
        if (debug) {
            storeData(bos, new FileOutputStream("old-content.xml")); //$NON-NLS-1$
        }

        applyTranformation(xslt, bos);
        if (debug) {
            storeData(bos, new FileOutputStream("changed-content.xml")); //$NON-NLS-1$
        } else {
            applyChanges(session, bos);
        }
    }

    private void applyChanges(Session session, ByteArrayOutputStream bos) {

    }

    private void storeData(ByteArrayOutputStream bos, FileOutputStream out)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory tmp = DocumentBuilderFactory.newInstance();
        tmp.setNamespaceAware(true);
        DocumentBuilder builder = tmp.newDocumentBuilder();
        Document doc = builder
                .parse(new ByteArrayInputStream(bos.toByteArray()));

        OutputFormat format = new OutputFormat();
        format.setLineWidth(65);
        format.setIndenting(true);
        format.setIndent(2);

        XMLSerializer serializer = new XMLSerializer(out, format);
        serializer.serialize(doc);
    }

    private void applyTranformation(String xslt, ByteArrayOutputStream bos)
            throws TransformerFactoryConfigurationError,
            TransformerConfigurationException, TransformerException {
        // JAXP reads data using the Source interface
        Source xmlSource = new StreamSource(new ByteArrayInputStream(bos
                .toByteArray()));
        Source xsltSource = new StreamSource(new File(xslt));

        ByteArrayOutputStream changeResult = new ByteArrayOutputStream();

        // the factory pattern supports different XSLT processors
        TransformerFactory transFact = TransformerFactory.newInstance();
        Transformer trans = transFact.newTransformer(xsltSource);
        trans.transform(xmlSource, new StreamResult(changeResult));
    }

    private ByteArrayOutputStream exportRepositoryContent(Session session)
            throws IOException, PathNotFoundException, RepositoryException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        session.exportDocumentView("/", bos, false, false); //$NON-NLS-1$
        return bos;
    }

    private Session login(String repoLocation, String login, String pwd)
            throws MalformedURLException, NotBoundException, RemoteException,
            LoginException, RepositoryException {
        ClientRepositoryFactory factory = new ClientRepositoryFactory();
        Repository repo = factory.getRepository(repoLocation);
        Session session = repo.login(new SimpleCredentials(login, pwd
                .toCharArray()));
        return session;
    }

    /**
     * Automatically generate and print the help statement.
     */
    private static void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar mindquarry-jcr-changes-<version>.jar", //$NON-NLS-1$
                options, true);
    }
}
