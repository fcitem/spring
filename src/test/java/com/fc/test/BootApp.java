/*
 * Copyright 2002-2017 the original author or authors.
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

package com.fc.test;

import java.io.IOException;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;


/**
 * 
 * @author fengchao
 * @since 4.2.1
 */
public class BootApp {

	@Test
	public void test() {
		ClassPathXmlApplicationContext context=new ClassPathXmlApplicationContext("classpath:com/fc/spring.xml");
		context.start();
		try {
			System.in.read();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Test
	public void test2() {
		BeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource(""),null);
		System.out.println(null instanceof Object);
		//获取加载bean
		beanFactory.getBean("test");
	}
	@Test
	public void test3() {
		ClassPathXmlApplicationContext beanFactory = new ClassPathXmlApplicationContext("");
		System.out.println(null instanceof Object);
		//获取加载bean
		beanFactory.getBean("test");
	}

}
