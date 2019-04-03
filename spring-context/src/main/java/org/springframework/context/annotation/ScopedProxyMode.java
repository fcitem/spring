/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.context.annotation;

/**
 * 各种代理选项枚举<br/>
 * Enumerates the various scoped-proxy options.
 *
 * <p>For a more complete discussion of exactly what a scoped proxy is, see the
 * section of the Spring reference documentation entitled '<em>Scoped beans as
 * dependencies</em>'.
 *
 * @author Mark Fisher
 * @see ScopeMetadata
 * @since 2.5
 */
public enum ScopedProxyMode {

    /**
     * 默认值<br/>
     * Default typically equals {@link #NO}, unless a different default
     * has been configured at the component-scan instruction level.
     */
    DEFAULT,

    /**
     * 不创建代理<br/>
     * Do not create a scoped proxy.
     * <p>This proxy-mode is not typically useful when used with a
     * non-singleton scoped instance, which should favor the use of the
     * {@link #INTERFACES} or {@link #TARGET_CLASS} proxy-modes instead if it
     * is to be used as a dependency.
     */
    NO,

    /**
     * 创建JDK动态代理<br/>
     * Create a JDK dynamic proxy implementing <i>all</i> interfaces exposed by
     * the class of the target object.
     */
    INTERFACES,

    /**
     * 创建CGLIB代理<br/>
     * Create a class-based proxy (uses CGLIB).
     */
    TARGET_CLASS;

}
