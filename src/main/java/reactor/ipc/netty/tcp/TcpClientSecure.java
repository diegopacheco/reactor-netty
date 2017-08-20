/*
 * Copyright (c) 2011-2017 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package reactor.ipc.netty.tcp;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;

import io.netty.bootstrap.Bootstrap;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import reactor.core.Exceptions;

/**
 * @author Stephane Maldini
 */
final class TcpClientSecure extends TcpClientOperator {

	final SslContext sslContext;
	final Duration   handshakeTimeout;

	TcpClientSecure(TcpClient client, SslContext sslContext,
			Duration handshakeTimeout) {
		super(client);
		this.sslContext = Objects.requireNonNull(sslContext, "sslContext");
		this.handshakeTimeout = Objects.requireNonNull(handshakeTimeout, "handshakeTimeout");
	}


	TcpClientSecure(TcpClient client,
			Consumer<? super SslContextBuilder> configurator,
			Duration handshakeTimeout) {
		super(client);
		Objects.requireNonNull(configurator, "configurator");
		this.handshakeTimeout = Objects.requireNonNull(handshakeTimeout, "handshakeTimeout");

		SslContextBuilder builder = SslContextBuilder.forClient();
		SslContext sslContext;
		try {
			configurator.accept(builder);
			sslContext = builder.build();
		}
		catch (Exception sslException) {
			throw Exceptions.propagate(sslException);
		}
		this.sslContext = sslContext;
	}

	@Override
	public Bootstrap configure() {
		return TcpUtils.updateSslSupport(source.configure(), sslContext, handshakeTimeout);
	}

	static final SslContext DEFAULT_SSL_CONTEXT;

	static {
		SslContext sslContext;
		try {
			sslContext = SslContextBuilder.forClient()
			                              .build();
		}
		catch (Exception e) {
			sslContext = null;
		}
		DEFAULT_SSL_CONTEXT = sslContext;
	}


	@Override
	public SslContext sslContext(){
		return sslContext;
	}
}
