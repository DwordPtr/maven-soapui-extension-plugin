/*
 * Copyright 2013 Thomas Bouffard (redfish4ktc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import org.ktc.soapui.maven.invoker.util.*;
import static org.ktc.soapui.maven.invoker.util.Check.*;

CheckBuildLog checker = new CheckBuildLog(basedir);
log("Verifying wrong soapui version");
checker.assertLogFileContains("ERROR [SoapUI] An error occured [No signature of method: static com.eviware.soapui.support.GroovyUtils.registerJdbcDriver() is applicable for argument types: (java.lang.String) values: [org.h2.Driver]], see error log for details");
log("The soapui version is wrong");

return true;