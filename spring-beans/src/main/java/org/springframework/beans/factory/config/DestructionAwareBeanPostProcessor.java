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

package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

/**BeanPostProcessor的子接口,增加了一个调用销毁函数前的回调<br>
 * Subinterface of {@link BeanPostProcessor} that adds a before-destruction callback.
 *<p> 典型的用法是调用特定bean类型的自定义销毁回调函数，匹配相应的初始化回调函数
 * <p>The typical usage will be to invoke custom destruction callbacks on
 * specific bean types, matching corresponding initialization callbacks.
 *
 * @author Juergen Hoeller
 * @since 1.0.1
 */
public interface DestructionAwareBeanPostProcessor extends BeanPostProcessor {

	/**在销毁之前将此BeanPostProcessor应用于给定的bean实例。 可以调用自定义销毁回调<br>
	 * Apply this BeanPostProcessor to the given bean instance before
	 * its destruction. Can invoke custom destruction callbacks.
	 * <p>Like DisposableBean's {@code destroy} and a custom destroy method,
	 * this callback just applies to singleton beans in the factory (including
	 * inner beans).
	 * @param bean the bean instance to be destroyed
	 * @param beanName the name of the bean
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @see org.springframework.beans.factory.DisposableBean
	 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#setDestroyMethodName
	 */
	void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException;

}
