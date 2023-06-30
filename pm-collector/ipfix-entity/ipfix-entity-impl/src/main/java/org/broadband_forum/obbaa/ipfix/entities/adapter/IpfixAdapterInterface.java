/*
 * Copyright 2023 Broadband Forum
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
 */

package org.broadband_forum.obbaa.ipfix.entities.adapter;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.broadband_forum.obbaa.ipfix.entities.exception.CollectingProcessException;
import org.broadband_forum.obbaa.ipfix.entities.message.IpfixMessage;
import org.broadband_forum.obbaa.ipfix.entities.message.logging.IpfixDecodedData;
import org.broadband_forum.obbaa.ipfix.entities.message.logging.IpfixLogging;
import org.broadband_forum.obbaa.ipfix.entities.set.IpfixDataSet;


public interface IpfixAdapterInterface {

    List<Set<IpfixDecodedData>> decodeIpfixMessage(List<IpfixDataSet> rawDataSets, Optional<String>[] optionalHostNames,
                 IpfixMessage ipfixMessage, IpfixLogging ipfixLogging, String deviceFamily) throws CollectingProcessException;

}
