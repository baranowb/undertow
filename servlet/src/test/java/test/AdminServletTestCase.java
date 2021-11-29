package test;

import static io.undertow.util.Headers.AUTHORIZATION;
import static io.undertow.util.Headers.BASIC;
import static org.junit.Assert.assertEquals;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.AuthMethodConfig;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.LoginConfig;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.test.security.constraint.ServletIdentityManager;
import io.undertow.servlet.test.util.TestClassIntrospector;
import io.undertow.testutils.DefaultServer;
import io.undertow.testutils.HttpClientUtils;
import io.undertow.testutils.TestHttpClient;
import io.undertow.util.FlexBase64;
import io.undertow.util.Headers;

@RunWith(DefaultServer.class)
public class AdminServletTestCase {
    static DeploymentManager manager;
    private static final String REALM_NAME = "Servlet_Realm-1";

    @BeforeClass
    public static void setup() throws ServletException {

        final PathHandler root = new PathHandler();
        final ServletContainer container = ServletContainer.Factory.newInstance();

        DeploymentInfo builder = new DeploymentInfo()
                .setClassLoader(AdminServletTestCase.class.getClassLoader())
                .setContextPath("/servletContext")
                .setClassIntrospecter(TestClassIntrospector.INSTANCE)
                .setDeploymentName("servletContext.war")
                .addListener(new ListenerInfo(ContextListener.class));

        final ServletIdentityManager identityManager = new ServletIdentityManager();
        identityManager.addUser("user1", "password1", "admin");

        LoginConfig loginConfig = new LoginConfig(REALM_NAME);
        Map<String, String> props = new HashMap<>();
        props.put("charset", "ISO_8859_1");
        props.put("user-agent-charsets", "Chrome,UTF-8,OPR,UTF-8");
        loginConfig.addFirstAuthMethod(new AuthMethodConfig("BASIC", props));

        builder.setIdentityManager(identityManager)
        .setLoginConfig(loginConfig);

        manager = container.addDeployment(builder);
        manager.deploy();
        root.addPrefixPath(builder.getContextPath(), manager.start());

        DefaultServer.setRootHandler(root);
    }

    @Test
    public void testRoles() throws Exception {
        final StringBuilder sb = new StringBuilder(40);
        sb.append("Admin resource");
        testCall(sb.toString(), StandardCharsets.UTF_8, "Chrome", "user1", "password1", 200);
    }

    public void testCall(final String expectedResponse, Charset charset, String userAgent, String user, String password,
            int expect) throws Exception {
        TestHttpClient client = new TestHttpClient();
        try {
            String url = DefaultServer.getDefaultServerURL() + "/servletContext/admin";
            HttpGet get = new HttpGet(url);
            get = new HttpGet(url);
            get.addHeader(Headers.USER_AGENT_STRING, userAgent);
            get.addHeader(AUTHORIZATION.toString(),
                    BASIC + " " + FlexBase64.encodeString((user + ":" + password).getBytes(charset), false));
            HttpResponse result = client.execute(get);
            assertEquals(expect, result.getStatusLine().getStatusCode());

            final String response = HttpClientUtils.readResponse(result);
            if (expect == 200) {
                assertEquals(expectedResponse, response);
            }
        } finally {
            client.getConnectionManager().shutdown();
        }
    }
}
