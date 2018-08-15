import appium.AppiumController;
import io.qameta.allure.*;
import io.qameta.allure.junit4.DisplayName;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.OutputType;
import screens.LoginScreen;
import screens.SearchResultsScreen;
import screens.SearchScreen;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

@RunWith(Parameterized.class)
public class TestPhoneLookup extends AppiumController {
    private final static String DEVICECONNECT_PROPERTIES_FILE = "deviceconnect.properties";
    private final static String DEVICECONNECT_URL = "deviceconnect.url";
    private final static String DEVICECONNECT_USERNAME = "deviceconnect.username";
    private final static String DEVICECONNECT_API_KEY = "deviceconnect.api.key";

    private final static String IOS_IDS = "ios.ids";
    private final static String IOS_BUNDLE_ID = "ios.bundle.id";
    private final static String IOS_PLATFORM_NAME = "IOS";
    private final static String IOS_AUTOMATION_NAME = "XCUITest";

    private final static String ANDROID_IDS = "android.ids";
    private final static String ANDROID_BUNDLE_ID = "android.bundle.id";
    private final static String ANDROID_PLATFORM_NAME = "ANDROID";
    private final static String ANDROID_AUTOMATION_NAME = "UiAutomator2";

    protected LoginScreen loginScreen;
    protected SearchScreen searchScreen;
    protected SearchResultsScreen searchResultsScreen;

    public TestPhoneLookup(String udid, String platformName,
                           String bundleID, String automationName) {
        this.udid = udid;
        this.bundleID = bundleID;
        this.automationName = automationName;
        this.platform = OperatingSystem.valueOf(platformName);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        //Convert the properties file information a list of devices
        deviceList = buildDeviceList();
        return deviceList;
    }

    @BeforeClass
    public static void beforeStart() throws Exception {
        //Load deviceConnect properties from file used for every test connection
        Properties props = new Properties();
        props.load(new FileReader(new File(DEVICECONNECT_PROPERTIES_FILE)));

        //Load the server connection properties
        server = props.getProperty(DEVICECONNECT_URL);
        username = props.getProperty(DEVICECONNECT_USERNAME);
        apiToken = props.getProperty(DEVICECONNECT_API_KEY);
    }

    @Before
    public void setUp() throws Exception {
        startAppium();

        loginScreen = new LoginScreen(driver, wait);
        searchScreen = new SearchScreen(driver, wait);
        searchResultsScreen = new SearchResultsScreen(driver, wait);
    }

    @After
    public void tearDown() throws Exception {
        stopAppium();
    }

    @Test
    @DisplayName("Verify Search")
    @Feature("Search")
    @Story("Valid Search")
    @Description("Verifies that the list of items is returned after filling out the search form")
    public void searchTest() throws Exception {
        try {
            loginScreen.login("mobilelabs", "demo");
            searchScreen.fillSearchForm("Droid Charge", "Samsung", true, true, false, false, "In Stock");
            Assert.assertTrue(searchResultsScreen.isSearchResultListPresent());
            getScreenshot("Search Results");
        } catch (Exception ex) {

            //Get screenshot if test fails
            getScreenshot("Failed - Exception");
            throw ex;
        }
    }

    @Attachment(value = "{attachmentName}", type = "image/png")
    public byte[] getScreenshot(String attachmentName) throws Exception {
        // make screenshot and get is as base64
        return driver.getScreenshotAs(OutputType.BYTES);
    }

    private static void buildDeviceList(List<Object[]> list, String deviceList, String bundleId,
                                        String platformName, String automationName) {
        if (deviceList != null && !deviceList.trim().isEmpty()) {
            for (String device : deviceList.split(",")) {
                list.add(new Object[]{
                        device.trim(), platformName, bundleId, automationName
                });
            }
        }
    }

    private static List<Object[]> buildDeviceList() throws IOException {
        List<Object[]> devices = new ArrayList<Object[]>();

        //Pull device properties from file
        //Used to run multiple devices in parallel
        Properties props = new Properties();
        props.load(new FileReader(new File(DEVICECONNECT_PROPERTIES_FILE)));

        //Load iOS devices from properties file
        buildDeviceList(devices, props.getProperty(IOS_IDS), props.getProperty(IOS_BUNDLE_ID),
                IOS_PLATFORM_NAME, IOS_AUTOMATION_NAME);

        //Load Android devices from properties file
        buildDeviceList(devices, props.getProperty(ANDROID_IDS), props.getProperty(ANDROID_BUNDLE_ID),
                ANDROID_PLATFORM_NAME, ANDROID_AUTOMATION_NAME);

        return devices;
    }
}