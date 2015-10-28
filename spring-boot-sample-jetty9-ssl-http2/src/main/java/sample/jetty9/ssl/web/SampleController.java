/*
 * Copyright 2012-2015 the original author or authors.
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

package sample.jetty9.ssl.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.server.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import sample.jetty9.ssl.service.HelloWorldService;

@Controller
public class SampleController {

	@Autowired
	private HelloWorldService helloWorldService;

	@RequestMapping("/")
	@ResponseBody
	public String helloWorld() {
		return this.helloWorldService.getHelloMessage();
	}

	@RequestMapping("/test")
	public void test(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			@RequestParam Map<String, Object> params, ModelMap modelMap) throws Exception {

		Request jettyRequest = (Request) request;

		if (jettyRequest.isPushSupported()) {
			jettyRequest.getPushBuilder().path("/favicon.ico").push();
			//Dispatcher dp = (Dispatcher) request.getRequestDispatcher("/favicon.ico");
			//dp.push(request);
		}

	}

}
