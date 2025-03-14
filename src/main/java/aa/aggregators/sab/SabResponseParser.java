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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static aa.aggregators.sab.SabInfoType.GUID;
import static aa.aggregators.sab.SabInfoType.MOBILE;
import static aa.aggregators.sab.SabInfoType.ORGANIZATION;
import static aa.aggregators.sab.SabInfoType.ROLE;
import static java.util.stream.IntStream.range;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

@SuppressWarnings("unchecked")
public class SabResponseParser {

    public Map<SabInfoType, List<String>> parse(Reader soap) throws XMLStreamException {
        //despite its name, the XMLInputFactoryImpl is not thread safe
        XMLInputFactory factory = XMLInputFactory.newInstance();

        XMLStreamReader reader = factory.createXMLStreamReader(soap);

        Map<SabInfoType, List<String>> result = new HashMap<>();
        SabInfoType sabInfoType = null;
        List<SabInfoType> sabInfoTypes = Arrays.asList(SabInfoType.values());

        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "Attribute":
                            Optional<SabInfoType> sabInfoTypeOptional =
                                sabInfoTypes.stream().filter(type -> hasAttributeValue(reader, type.getUrn())).findFirst();
                            sabInfoType = sabInfoTypeOptional.orElse(null);
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

    private boolean hasAttributeValue(XMLStreamReader reader, String value) {
        return range(0, reader.getAttributeCount())
            .mapToObj(i -> reader.getAttributeValue(i)).anyMatch(v -> v != null && v.equals(value));
    }
}
