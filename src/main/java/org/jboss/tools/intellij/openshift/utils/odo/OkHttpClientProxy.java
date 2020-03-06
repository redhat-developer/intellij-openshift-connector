package org.jboss.tools.intellij.openshift.utils.odo;

import com.google.common.collect.Sets;
import com.intellij.util.net.HttpConfigurable;
import com.intellij.util.net.IdeaWideAuthenticator;
import com.intellij.util.net.IdeaWideProxySelector;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.intellij.openapi.util.text.StringUtil.isNotEmpty;
import static okhttp3.Credentials.basic;

public class OkHttpClientProxy {
	private OkHttpClient client;

	public OkHttpClient getClient() {
		return client;
	}

	public OkHttpClientProxy() {
		final HttpConfigurable httpConfigurable = HttpConfigurable.getInstance();
		final IdeaWideProxySelector ideaWideProxySelector = new IdeaWideProxySelector(httpConfigurable);
		final IdeaWideAuthenticator ideaWideAuthenticator = new IdeaWideAuthenticator(httpConfigurable);
		final okhttp3.Authenticator proxyAuthenticator = getProxyAuthenticator(ideaWideAuthenticator);
		final OkHttpClient.Builder builder = new OkHttpClient.Builder();

		builder.proxySelector(ideaWideProxySelector)
			.proxyAuthenticator(proxyAuthenticator);

		this.client = builder.build();
	}

	private static okhttp3.Authenticator getProxyAuthenticator(IdeaWideAuthenticator ideaWideAuthenticator) {
		okhttp3.Authenticator proxyAuthenticator = null;

		if (Objects.nonNull(ideaWideAuthenticator)) {
			proxyAuthenticator = (route, response) -> {
				final PasswordAuthentication authentication = ideaWideAuthenticator.getPasswordAuthentication();
				final String credential = basic(authentication.getUserName(), Arrays.toString(authentication.getPassword()));
				return response.request().newBuilder()
					.header("Proxy-Authorization", credential)
					.build();
			};
		}

		return proxyAuthenticator;
	}

	@NotNull
	public static Map<String, String> buildEnvironmentVariables(String url) throws URISyntaxException {
		final int FIRST = 0;
		final String HTTP_PROXY = "HTTP_PROXY";
		final String HTTPS_PROXY = "HTTPS_PROXY";
		final String ALL_PROXY = "ALL_PROXY";
		final Set<String> proxyEnvironmentVariables = Sets.newHashSet(HTTP_PROXY, HTTPS_PROXY, ALL_PROXY);

		final Map<String, String> environmentVariables = new HashMap<>(6);

		final OkHttpClientProxy okHttpClientProxy = new OkHttpClientProxy();
		final OkHttpClient client = okHttpClientProxy.client;
		final URI uri = new URI(url);
		final ProxySelector proxySelector = client.proxySelector();
		final List<Proxy> proxies = proxySelector.select(uri);

		if (!proxies.isEmpty()) {
			final Proxy proxy = proxies.get(FIRST);
			final Proxy.Type type = proxy.type();

			switch (type) {
				case HTTP:
				case SOCKS:
					final SocketAddress address = proxy.address();

					if (address instanceof InetSocketAddress) {
						final InetSocketAddress socketAddress = (InetSocketAddress) address;
						final InetAddress inetAddress = socketAddress.getAddress();
						final int port = socketAddress.getPort();

						final HttpConfigurable httpConfigurable = HttpConfigurable.getInstance();
						final IdeaWideAuthenticator ideaWideAuthenticator = new IdeaWideAuthenticator(httpConfigurable);
						final Optional<PasswordAuthentication> optionalPasswordAuthentication = Optional.ofNullable(ideaWideAuthenticator.getPasswordAuthentication());

						String userName = null;
						String password = null;
						if(optionalPasswordAuthentication.isPresent()) {
							final PasswordAuthentication passwordAuthentication = optionalPasswordAuthentication.get();
							userName = passwordAuthentication.getUserName();
							password = Arrays.toString(passwordAuthentication.getPassword());
						}

						String finalUserName = userName;
						String finalPassword = password;
						proxyEnvironmentVariables.forEach(envVar -> {
							final String httpProxy = buildHttpProxy(type, finalUserName, finalPassword, inetAddress, port);

							environmentVariables.put(envVar, httpProxy);
							environmentVariables.put(envVar.toLowerCase(), httpProxy);
						});
					}
					break;
			}
		}

		return environmentVariables;
	}

	@NotNull
	private static String buildHttpProxy(Proxy.Type type, String userName, String password, InetAddress address, int port) {
		final StringBuilder builder = new StringBuilder();

		switch (type) {
			case HTTP:
				builder.append("http://");
				break;
			case SOCKS:
				builder.append("socks://");
				break;
		}

		if (isNotEmpty(userName) && isNotEmpty(password)) {
			builder.append(userName).append(":").append(password).append("@");
		}

		builder.append(address.getHostAddress()).append(":").append(port);

		return builder.toString();
	}
}
