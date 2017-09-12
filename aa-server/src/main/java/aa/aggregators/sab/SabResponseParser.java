/*
 * Copyright 2013 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package aa.aggregators.sab;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.IntStream.range;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

@SuppressWarnings("unchecked")
public class SabResponseParser {

    public Map<SabInfoType, List<String>> parse(Reader soap) throws XMLStreamException {
        //despite it's name, the XMLInputFactoryImpl is not thread safe
        XMLInputFactory factory = XMLInputFactory.newInstance();

        XMLStreamReader reader = factory.createXMLStreamReader(soap);

        Map<SabInfoType, List<String>> result = new HashMap<>();
        SabInfoType sabInfoType = null;

        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "Attribute":
                            if (hasAttributeValue(reader, "urn:oid:1.3.6.1.4.1.5923.1.1.1.7")) {
                                sabInfoType = SabInfoType.ROLE;
                            } else if (hasAttributeValue(reader, "urn:oid:1.3.6.1.4.1.1076.20.100.10.50.1")) {
                                sabInfoType = SabInfoType.ORGANIZATION;
                            } else if (hasAttributeValue(reader, "urn:oid:1.3.6.1.4.1.1076.20.100.10.50.2")) {
                                sabInfoType = SabInfoType.GUID;
                            } else {
                                sabInfoType = null;
                            }
                            break;
                        case "AttributeValue":
                            if (sabInfoType != null) {
                                List<String> subResult = result.computeIfAbsent(sabInfoType, type -> new ArrayList());
                                subResult.add(reader.getElementText().trim());
                            }
                            break;
                    }
                    break;
            }
        }
        return result;
    }

    private boolean hasAttributeValue(XMLStreamReader reader, String attributeValue) {
        return range(0, reader.getAttributeCount()).mapToObj(i -> reader.getAttributeValue(i)).anyMatch(v -> v != null && v.equals(attributeValue));
    }
}
