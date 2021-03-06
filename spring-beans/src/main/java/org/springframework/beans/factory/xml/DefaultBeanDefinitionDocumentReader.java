/*
 * Copyright 2002-2015 the original author or authors.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Default implementation of the {@link BeanDefinitionDocumentReader} interface that
 * reads bean definitions according to the "spring-beans" DTD and XSD format
 * (Spring's default XML bean definition format).
 *
 * <p>The structure, elements, and attribute names of the required XML document
 * are hard-coded in this class. (Of course a transform could be run if necessary
 * to produce this format). {@code <beans>} does not need to be the root
 * element of the XML document: this class will parse all bean definition elements
 * in the XML file, regardless of the actual root element.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Erik Wiersma
 * @since 18.12.2003
 */
public class DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader {

    public static final String BEAN_ELEMENT = BeanDefinitionParserDelegate.BEAN_ELEMENT;

    public static final String NESTED_BEANS_ELEMENT = "beans";

    public static final String ALIAS_ELEMENT = "alias";

    public static final String NAME_ATTRIBUTE = "name";

    public static final String ALIAS_ATTRIBUTE = "alias";

    public static final String IMPORT_ELEMENT = "import";

    public static final String RESOURCE_ATTRIBUTE = "resource";

    public static final String PROFILE_ATTRIBUTE = "profile";


    protected final Log logger = LogFactory.getLog(getClass());

    private XmlReaderContext readerContext;
    /**定义解析Element的各种方法*/
    private BeanDefinitionParserDelegate delegate;


    /**
     * This implementation parses bean definitions according to the "spring-beans" XSD
     * (or DTD, historically).
     * <p>Opens a DOM Document; then initializes the default settings
     * specified at the {@code <beans/>} level; then parses the contained bean definitions.
     */
    @Override
    public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
        //readerContext赋值,后面要用
        this.readerContext = readerContext;
        logger.debug("Loading bean definitions");
        //获取root根元素
        Element root = doc.getDocumentElement();
        //在给定的根<beans />元素内注册每个bean定义及真正的解析开始
        doRegisterBeanDefinitions(root);
    }

    /**
     * Return the descriptor for the XML resource that this parser works on.
     */
    protected final XmlReaderContext getReaderContext() {
        return this.readerContext;
    }

    /**
     * Invoke the {@link org.springframework.beans.factory.parsing.SourceExtractor} to pull the
     * source metadata from the supplied {@link Element}.
     */
    protected Object extractSource(Element ele) {
        return getReaderContext().extractSource(ele);
    }


    /**
     * Register each bean definition within the given root {@code <beans/>} element.
     */
    protected void doRegisterBeanDefinitions(Element root) {
        // Any nested <beans> elements will cause recursion in this method. In
        // order to propagate and preserve <beans> default-* attributes correctly,
        // keep track of the current (parent) delegate, which may be null. Create
        // the new (child) delegate with a reference to the parent for fallback purposes,
        // then ultimately reset this.delegate back to its original (parent) reference.
        // this behavior emulates a stack of delegates without actually necessitating one.
        BeanDefinitionParserDelegate parent = this.delegate;
        //创建bean definition的委托代理类,并初初始化委托类的默认配置
        this.delegate = createDelegate(getReaderContext(), root, parent);
        //如果为<beans>标签
        if (this.delegate.isDefaultNamespace(root)) {
            //解析处理profile
            String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
            if (StringUtils.hasText(profileSpec)) {
                //是否有profile属性配置
                String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
                        profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
                //验证profile是否符合环境变量中所定义的
                if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
                    return;
                }
            }
        }
        //处理xml前执行，这儿为空,留给子类实现
        preProcessXml(root);
        //解析bean definition从root开始
        parseBeanDefinitions(root, this.delegate);
        //处理xml后执行，这儿为空,留给子类实现
        postProcessXml(root);
        //到此为止完成了spring对bean配置文件的全部解析,也就是将bean从配置文件描述转换加载到了内存当中
        this.delegate = parent;
        //接下来是bean的真正获取使用流程
    }

    /**
     * 创建一个bean definition也就是XmlReaderContext的委托类
     *
     * @param readerContext
     * @param root
     * @param parentDelegate
     * @return
     */
    protected BeanDefinitionParserDelegate createDelegate(
            XmlReaderContext readerContext, Element root, BeanDefinitionParserDelegate parentDelegate) {

        //根据readerContext创建一个委托代理类
        BeanDefinitionParserDelegate delegate = new BeanDefinitionParserDelegate(readerContext);
        //初始化bean definition默认设置
        delegate.initDefaults(root, parentDelegate);
        return delegate;
    }

    /**
     * Parse the elements at the root level in the document:
     * "import", "alias", "bean".
     *
     * @param root the DOM root element of the document
     */
    protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
        //如果为beans标签
        if (delegate.isDefaultNamespace(root)) {
            NodeList nl = root.getChildNodes();
            //对beans下面的每个元素循环解析
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (node instanceof Element) {
                    Element ele = (Element) node;
                    //对于根节点或者子节点如果是默认命名空间(只有beans标签)的话
                    if (delegate.isDefaultNamespace(ele)) {
                        //解析在默认的root(beans)下面的标签
                        parseDefaultElement(ele, delegate);
                    }
                    //自定义命名空间的解析
                    else {
                        //解析自定义标签也是走这儿
                        delegate.parseCustomElement(ele);
                    }
                }
            }
        } else {
            delegate.parseCustomElement(root);
        }
    }

    /**
     * 解析import,alias,bean等标签
     *
     * @param ele
     * @param delegate
     */
    private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
        if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
            //import处理
            importBeanDefinitionResource(ele);
        } else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
            //alias处理
            processAliasRegistration(ele);
        } else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
            //bean处理
            processBeanDefinition(ele, delegate);
        } else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
            // 内嵌式beans标签处理
            //如果等于beans标签继续递归调用
            doRegisterBeanDefinitions(ele);
        }
    }

    /**
     * 解析import标签，加载bean definition</br>
     * Parse an "import" element and load the bean definitions
     * from the given resource into the bean factory.
     */
    protected void importBeanDefinitionResource(Element ele) {
        String location = ele.getAttribute(RESOURCE_ATTRIBUTE);
        //验证import标签里面是否包含了resource属性
        if (!StringUtils.hasText(location)) {
            getReaderContext().error("Resource location must not be empty", ele);
            return;
        }

        // Resolve system properties: e.g. "${user.dir}"
        //解析系统属性,例如 ${user.dir}
        location = getReaderContext().getEnvironment().resolveRequiredPlaceholders(location);

        //已经被解析过的资源
        Set<Resource> actualResources = new LinkedHashSet<Resource>(4);

        // Discover whether the location is an absolute or relative URI
        //确定location是否是绝对路径
        boolean absoluteLocation = false;
        try {
            absoluteLocation = ResourcePatternUtils.isUrl(location) || ResourceUtils.toURI(location).isAbsolute();
        } catch (URISyntaxException ex) {
            // cannot convert to an URI, considering the location relative
            // unless it is the well-known Spring prefix "classpath*:"
        }

        // Absolute or relative?
        if (absoluteLocation) {
            try {
                //获取当前location下面的resource数量,递归调用
                int importCount = getReaderContext().getReader().loadBeanDefinitions(location, actualResources);
                if (logger.isDebugEnabled()) {
                    logger.debug("Imported " + importCount + " bean definitions from URL location [" + location + "]");
                }
            } catch (BeanDefinitionStoreException ex) {
                getReaderContext().error(
                        "Failed to import bean definitions from URL location [" + location + "]", ele, ex);
            }
        } else {
            // No URL -> considering resource location as relative to the current file.
            try {
                int importCount;
                //根据相对位置计算绝对位置
                Resource relativeResource = getReaderContext().getResource().createRelative(location);
                if (relativeResource.exists()) {
                    importCount = getReaderContext().getReader().loadBeanDefinitions(relativeResource);
                    actualResources.add(relativeResource);
                } else {
                    String baseLocation = getReaderContext().getResource().getURL().toString();
                    importCount = getReaderContext().getReader().loadBeanDefinitions(
                            StringUtils.applyRelativePath(baseLocation, location), actualResources);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Imported " + importCount + " bean definitions from relative location [" + location + "]");
                }
            } catch (IOException ex) {
                getReaderContext().error("Failed to resolve current resource location", ele, ex);
            } catch (BeanDefinitionStoreException ex) {
                getReaderContext().error("Failed to import bean definitions from relative location [" + location + "]",
                        ele, ex);
            }
        }
        Resource[] actResArray = actualResources.toArray(new Resource[actualResources.size()]);
        getReaderContext().fireImportProcessed(location, actResArray, extractSource(ele));
    }

    /**
     * 解析alias标签,并注册到工厂中<br>
     * Process the given alias element, registering the alias with the registry.
     */
    protected void processAliasRegistration(Element ele) {
        String name = ele.getAttribute(NAME_ATTRIBUTE);
        String alias = ele.getAttribute(ALIAS_ATTRIBUTE);
        boolean valid = true;
        if (!StringUtils.hasText(name)) {
            getReaderContext().error("Name must not be empty", ele);
            valid = false;
        }
        if (!StringUtils.hasText(alias)) {
            getReaderContext().error("Alias must not be empty", ele);
            valid = false;
        }
        if (valid) {
            try {
                //注册别名
                getReaderContext().getRegistry().registerAlias(name, alias);
            } catch (Exception ex) {
                getReaderContext().error("Failed to register alias '" + alias +
                        "' for bean with name '" + name + "'", ele, ex);
            }
            getReaderContext().fireAliasRegistered(name, alias, extractSource(ele));
        }
    }

    /**
     * bean标签的解析方法,默认标签解析及提取到BeanDefinition</br>
     * Process the given bean element, parsing the bean definition
     * and registering it with the registry.
     */
    protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
        //开始解析元素标签，委托代理类进行
        BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
        if (bdHolder != null) {
            //查找对标签下的自定义标签，再次解析,这里针对的是自定义标签不是一个Bean的情况,自定义类型是属性的情况
            bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
            try {
                // Register the final decorated instance.
                //注册最终的holder实例
                BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
            } catch (BeanDefinitionStoreException ex) {
                getReaderContext().error("Failed to register bean definition with name '" +
                        bdHolder.getBeanName() + "'", ele, ex);
            }
            // Send registration event.
            //发送注册事件,这儿只为扩展,spring未做任何处理,开发人员可监听此事件做相应处理
            getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
        }
    }


    /**
     * Allow the XML to be extensible by processing any custom element types first,
     * before we start to process the bean definitions. This method is a natural
     * extension point for any other custom pre-processing of the XML.
     * <p>The default implementation is empty. Subclasses can override this method to
     * convert custom elements into standard Spring bean definitions, for example.
     * Implementors have access to the parser's bean definition reader and the
     * underlying XML resource, through the corresponding accessors.
     *
     * @see #getReaderContext()
     */
    protected void preProcessXml(Element root) {
    }

    /**
     * Allow the XML to be extensible by processing any custom element types last,
     * after we finished processing the bean definitions. This method is a natural
     * extension point for any other custom post-processing of the XML.
     * <p>The default implementation is empty. Subclasses can override this method to
     * convert custom elements into standard Spring bean definitions, for example.
     * Implementors have access to the parser's bean definition reader and the
     * underlying XML resource, through the corresponding accessors.
     *
     * @see #getReaderContext()
     */
    protected void postProcessXml(Element root) {
    }

}
