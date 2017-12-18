/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.xml;

/**被DefaultBeanDefinitionDocumentReader使用以定位一个特定namespace uri的NamespaceHandler实现<br>
 * Used by the {@link org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader} to
 * locate a {@link NamespaceHandler} implementation for a particular namespace URI.
 *
 * @author Rob Harrop
 * @since 2.0
 * @see NamespaceHandler
 * @see org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader
 */
public interface NamespaceHandlerResolver {

	/**解析namespace URI并返回这个uri对应的namespacehandler实现<br>
	 * Resolve the namespace URI and return the located {@link NamespaceHandler}
	 * implementation.
	 * @param namespaceUri the relevant namespace URI
	 * @return the located {@link NamespaceHandler} (may be {@code null})
	 */
	NamespaceHandler resolve(String namespaceUri);

}
