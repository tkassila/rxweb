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

package rxweb.server;

import java.util.List;
import java.util.stream.Collectors;

import org.reactivestreams.Publisher;
import reactor.Environment;
import reactor.io.net.NetChannel;
import reactor.io.net.tcp.TcpServer;
import reactor.rx.Promise;
import reactor.rx.Streams;
import rxweb.Server;
import rxweb.converter.BufferConverter;
import rxweb.converter.ByteBufferConverter;
import rxweb.converter.Converter;
import rxweb.converter.ConverterResolver;
import rxweb.converter.DefaultConverterResolver;
import rxweb.converter.JacksonJsonConverter;
import rxweb.converter.StringConverter;
import rxweb.http.Method;
import rxweb.http.Request;
import rxweb.mapping.Condition;
import rxweb.mapping.HandlerResolver;
import rxweb.mapping.MappingCondition;

/**
 * @author Sebastien Deleuze
 */
public abstract class AbstractServer implements Server {

	protected final Environment env = new Environment();
	protected final HandlerResolver handlerResolver = new DefaultHandlerResolver();
	protected final ConverterResolver converterResolver = new DefaultConverterResolver();
	protected TcpServer<ServerRequest, Object> server;
	protected String host = "0.0.0.0";
	protected int port = 8080;

	public AbstractServer() {
		initConverters();
	}

	public AbstractServer(String host, int port) {
		this.host = host;
		this.port = port;
		initConverters();
	}

	protected void handleRequest(NetChannel<ServerRequest, Object> ch, ServerRequest request)  {
		ServerResponse response = createResponse(ch, request);
		List<Publisher<?>> publishers = this.handlerResolver.resolve(request).stream().map(handler -> {
			request.setConverterResolver(this.converterResolver);
			handler.handle(request, response);
			return response.getContent();
		}).collect(Collectors.toList());

		// TODO: what behavior do we want to implement?
		// Currently implemented: Streams.concat(publishers) execute handlers in order (handler chain)
		// Also possible: Streams.merge(publishers) all handlers contribute to the output stream
		// (be careful if on the same single threaded dispatcher it is still executed sequentially,
		// so maybe we need to specify another dispatcher if we decide to implement an "all handlers
		// contribute at the same time" behavior)
		Streams.concat(publishers).map(data -> {
			// TODO: handle media type
			Converter converter = this.converterResolver.resolveWriter(data.getClass(), null);
			return converter.write(data, null);
		}).flatMap(buffer -> {
			if (response.isStatusAndHeadersSent()) {
				return ch.send(buffer);
			}
			else {
				response.setStatusAndHeadersSent(true);
				return Streams.concat(ch.send(response), ch.send(buffer));
			}
		}).consume(
				/* ignore onNext */ null,
				/* ignore errors */ Throwable::printStackTrace,
						/* close on Complete */ v -> {
			if (response.isStatusAndHeadersSent()) {
				ch.close();
			} else {
				response.setStatusAndHeadersSent(true);
				ch.send(response).onComplete(w -> ch.close());
			}
		});
	}

	protected abstract ServerResponse createResponse(NetChannel<ServerRequest, Object> ch, ServerRequest request);

	protected void initConverters() {
		this.converterResolver.addConverter(new BufferConverter());
		this.converterResolver.addConverter(new ByteBufferConverter());
		this.converterResolver.addConverter(new StringConverter());
		this.converterResolver.addConverter(new JacksonJsonConverter());
	}

	@Override
	public void addHandler(final Condition<Request> condition, final ServerHandler handler) {
		this.handlerResolver.addHandler(condition, handler);
	}

	@Override
	public void get(final String path, final ServerHandler handler) {
		addHandler(MappingCondition.Builder.from(path).method(Method.GET).build(), handler);
	}

	@Override
	public void post(final String path, final ServerHandler handler) {
		addHandler(MappingCondition.Builder.from(path).method(Method.POST).build(), handler);
	}

	@Override
	public void put(final String path, final ServerHandler handler) {
		addHandler(MappingCondition.Builder.from(path).method(Method.PUT).build(), handler);
	}

	@Override
	public void delete(final String path, final ServerHandler handler) {
		addHandler(MappingCondition.Builder.from(path).method(Method.DELETE).build(), handler);
	}

	@Override
	public Promise<Void> start() {
		return this.server.start();
	}

	@Override
	public Promise<Void> stop() {
		return this.server.shutdown();
	}

}
