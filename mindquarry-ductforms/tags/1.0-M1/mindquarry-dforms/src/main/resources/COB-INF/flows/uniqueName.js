/*
 * Copyright (C) 2006-2007 Mindquarry GmbH, All Rights Reserved
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 */

// just a dummy implementation
// available variables are
//   baseURI_ => the base URI before the documentID
//   suffix_ => the file suffix (typically .xml)
//   form_ => the javascript cocoon form object
function createUniqueName(baseURI) {
    return "dforms_" + baseURI;
}

createUniqueName(baseURI_);