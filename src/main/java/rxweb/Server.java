/*
 * Copyright 2002-2014 the original author or authors.
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

package rxweb;

import reactor.rx.Promise;
import rxweb.http.Request;
import rxweb.mapping.HandlerRegistry;
import rxweb.server.ServerHandler;

/**
 * @author Sebastien Deleuze
 */
public interface Server extends HandlerRegistry<Request> {

	Promise<Boolean> start();

	Promise<Boolean> stop();

	void setHost(String host);

	void setPort(int port);

	void get(final String path, final ServerHandler handler);

	<T> void get(final String path, final Class<T> requestClass, final ServerHandler handler);

	void post(final String path, final ServerHandler handler);

	<T> void post(final String path, final Class<T> requestClass, final ServerHandler handler);

	void put(final String path, final ServerHandler handler);

	<T> void put(final String path, final Class<T> requestClass, final ServerHandler handler);

	void delete(final String path, final ServerHandler handler);

	<T> void delete(final String path, final Class<T> requestClass, final ServerHandler handler);

}
