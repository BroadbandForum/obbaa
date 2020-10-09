/*
 * Copyright 2020 Broadband Forum
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
 * Includes OID types enum definition.
 *
 * Created by Balaji Venkatachalapathy (DZSI) on 01/10/2020.
 */

package org.broadband_forum.obbaa.protocol.snmp;

/*
Enum for type of OIDs
*/
public enum OIDType {
    INTEGER32,
    UNSIGNED32,
    COUNTER32,
    OCTETSTRING,
    BITSTRING,
    GAUGE32;  //add more types as per requirement
}
