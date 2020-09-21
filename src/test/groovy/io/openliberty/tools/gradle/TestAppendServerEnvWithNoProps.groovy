package io.openliberty.tools.gradle

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import java.io.BufferedReader;
import java.io.FileReader;


import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class TestAppendServerEnvWithNoProps extends AbstractIntegrationTest {
    static File resourceDir = new File("build/resources/test/sample.servlet")
    static File buildDir = new File(integTestDir, "/test-append-server-env-with-no-props")
    static String buildFilename = "testAppendServerEnvWithNoEnvProps.gradle"

    @BeforeClass
    public static void setup() {
        createDir(buildDir)
        createTestProject(buildDir, resourceDir, buildFilename)
        runTasks(buildDir, 'libertyCreate')
    }

    @Test
    public void check_for_server_env() {
        assert new File('build/testBuilds/test-append-server-env-with-no-props/build/wlp/usr/servers/LibertyProjectServer/server.env').exists() : 'server.env not found!'
    }

    /*
        # default server.env
        keystore_password=sfKRrA1ioLdtIFQC9bEfkua

    */
    @Test
    public void check_server_env_contents() {
        File serverEnv = new File("build/testBuilds/test-append-server-env-with-no-props/build/wlp/usr/servers/LibertyProjectServer/server.env")
        FileInputStream input = new FileInputStream(serverEnv)
        
        Map<String,String> serverEnvContents = new HashMap<String,String>();

        BufferedReader bf = new BufferedReader(new FileReader(serverEnv))
        String line = bf.readLine();
        while(line != null) {
            //ignore comment lines
            if(!line.startsWith("#")) {
                String[] keyValuePair = line.split("=");
                String key = keyValuePair[0];
                String value = keyValuePair[1];

                serverEnvContents.put(key,value);
            } else {
                Assert.assertFalse("File should not have been generated by liberty-gradle-plugin", line.contains("liberty-gradle-plugin"));
            }
            line = bf.readLine();
        }
        
        // The contents of the default server.env can change over time.
        // After 20.0.0.3, for example, the WLP_SKIP_MAXPERMSIZE was removed.
        // Just confirm the keystore_password is present to prove the default server.env was merged with the plugin config.
        Assert.assertTrue("Number of env properties should be <= 2, but is "+serverEnvContents.size(),  	serverEnvContents.size() <= 2)
        Assert.assertTrue("keystore_password mapping found", serverEnvContents.containsKey("keystore_password"))

    }

}