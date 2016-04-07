import java.io.IOException;
import java.net.URL;
import java.rmi.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
//import org.openqa.selenium.firefox.FirefoxDriver;



public class FindingEmailChallenge {
    private static LinksDb linkDb;//contains all links
    private static LinksDb visitedLinkDb ;
    private static LinksDb subLinks;
    private static LinksDb visiedAttr;
    private static ChromeDriverService service;
    private static WebDriver driver;
    private static int count=0;

    public static ArrayList<String> matchEmail(ArrayList<String> emails,Document doc){
        Pattern p = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");
        Matcher matcher = p.matcher(doc.text());
        while (matcher.find()) {
            emails.add(matcher.group());
        }
        return emails;
    }
    
    public static ArrayList<String> findEmail(ArrayList<String> emails, String url){
        String validatedMainUrl = url;
        ArrayList<String> linksInPage = new ArrayList<String>();
        List<WebElement> linkListsOnPage;//links found by webdriver
        List<WebElement> emailListsOnPage = new ArrayList<WebElement>();
        List<String> allAttr = new ArrayList<String>();//all wanted ng-click attrs
        String va=validatedMainUrl+"/";
        

        
        if(url.startsWith("http://")==false){
            validatedMainUrl="http://"+url;
        }

        driver.get(validatedMainUrl);

        linkDb.addToDb(validatedMainUrl);
        linkDb.addToDb(va);
        visitedLinkDb.addToDb(validatedMainUrl);
        visitedLinkDb.addToDb(va);
        subLinks.addToDb(validatedMainUrl);
        subLinks.addToDb(va);

        String ngApp =  driver.findElement(By.tagName("html")).getAttribute("ng-app");
        if(ngApp!=null){
//              <----------Start || Webdriver || To get all ng-clicks--------------------->
            
            
            linkListsOnPage = driver.findElements(By.tagName("span"));
            
            for(WebElement l :linkListsOnPage){//store all ng-click attrs value 
                if(l.getAttribute("ng-click")!=null){
                    allAttr.add(l.getAttribute("ng-click"));
                }
                
            }
            for(String attr :allAttr){
                try {
                    driver.get(validatedMainUrl);//make sure to stay on page 
                    List<WebElement> eles = driver.findElements(By.tagName("span"));
                    WebElement foundEleByAttr = null;
                    
                    for(int i = 0;i<eles.size();i++){//find that ele
                        WebElement ele=eles.get(i);
                        if(ele.getAttribute("ng-click")!=null ){
                            if(ele.getAttribute("ng-click").equals(attr)){
                                foundEleByAttr = ele;
                            }
                        }
                    }
                    if(foundEleByAttr!=null && visiedAttr.isInPool(foundEleByAttr.getAttribute("ng-click"))==false){
                        JavascriptExecutor js = (JavascriptExecutor)driver;

                        js.executeScript("arguments[0].click();", foundEleByAttr);
                        visiedAttr.addToDb(foundEleByAttr.getAttribute("ng-click"));
                    }
                    
                    Thread.sleep(100);
                    
                    if(linkDb.isInPool(driver.getCurrentUrl())==false){//if directed page was not visited  
                        String driverUrl = driver.getCurrentUrl();
                        linkDb.addToDb(driverUrl);
                        linksInPage.add(driverUrl);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(FindingEmailChallenge.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
//        <-----------------EDN || Webdriver || Got all links that triggered by button clicks-------------->  
        }
//        
//        
//        <-----------------Start || Jsoup || Paring all links -------------------------------------------->
        try {

            Document doc = Jsoup.connect(validatedMainUrl).get();
            Connection.Response redUrl = Jsoup.connect(validatedMainUrl).followRedirects(true).execute();
            String redURL = redUrl.url().toString();
            matchEmail(emails,doc);
            
            Elements allLinks = doc.select("a");

            for (Element e : allLinks){//add direct links to arrylist
                String webUrl = e.attr("href");

                if(linkDb.isInPool(webUrl)==false){
                    linkDb.addToDb(webUrl);
                }
                linksInPage.add(webUrl);//added this link to Lists of links that found on current page
            }
            for (String u : linksInPage) {// loop through All found links

                Connection.Response response = null;
                String excutedUrl = "";

                if(u.contains(redURL)==true && visitedLinkDb.isInPool(u)==false){//filter out the url that is not subpage && is not in the linkDB

                    try{
                        response = Jsoup.connect(u).followRedirects(true).execute();//exxcute url and get final url
                        excutedUrl = response.url().toString();
                    }   
                    catch (UnknownHostException error) {
                        System.err.println("Unknown host");
                        error.printStackTrace(); // I'd rather (re)throw it though.
                    }
                    if (excutedUrl.startsWith(redURL) && visitedLinkDb.isInPool(excutedUrl)==false){//filter out the url that is not subpage after redirect

                        subLinks.addToDb(excutedUrl);
                        linkDb.addToDb(excutedUrl);
                        count++;
                        findEmail(emails,excutedUrl);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(FindingEmailChallenge.class.getName()).log(Level.SEVERE, null, ex);
        }
        return emails;
    }

    public static void main(String[] args) {

        System.setProperty("webdriver.chrome.driver", "src/findingemailchallenge/chromedriver");
        FindingEmailChallenge.driver = new ChromeDriver();
        linkDb = new LinksDb();
        visitedLinkDb = new LinksDb();
        visiedAttr = new LinksDb();
        subLinks = new LinksDb();
        
        ArrayList<String> emails = new ArrayList<String>();
//         findEmail(emails,args[0]);

       findEmail(emails,"mit.edu");
//        findEmail(emails,"jana.com");
        
//        findEmail(emails,"http://techcrunch.com/");
        linkDb.printDb();
        for(String em:emails){
            System.out.println(em);
        }
        
        
    }


}
