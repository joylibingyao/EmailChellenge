import java.io.IOException;
import java.net.URL;
import java.rmi.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
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



public class FindingEmailChallenge {
    private static int count=0;
    private static LinksDb linkDb;
   
    
    public static ArrayList<String> findEmail(ArrayList<String> emails, String url){
        String validatedMainUrl = url;
        ArrayList<String> linksInPage = new ArrayList<String>();
        if(url.startsWith("http://")==false){
                validatedMainUrl="http://"+url;
            }
        linkDb.getLinksDb().add(validatedMainUrl);//add current url to linkPool
        
        try {
            
            Document doc = Jsoup.connect(validatedMainUrl).get();
            Connection.Response redUrl = Jsoup.connect(validatedMainUrl).followRedirects(true).execute();
            String redURL = redUrl.url().toString();
            
            Pattern emailForm = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");
            Matcher matcher = emailForm.matcher(doc.text());
            
            
//            System.out.println("get node's link "+linkNode.arrSize());
            
            while (matcher.find()) {
                System.out.println("this matches "+matcher.group());
                emails.add(matcher.group());
            }
            Elements elements = doc.select("a");

            for (Element e : elements) {// loop through All found links
                String webUrl = e.attr("href");//the Url
                Connection.Response response = null;
                String excutedUrl = "";
                
//                System.out.println("before  "+webUrl+webUrl.contains(redURL));
                if(webUrl.contains(redURL)==true && linkDb.isInPool(webUrl)==false){//filter out the url that is not subpage && is not in the linkDB
//                    thisUrl="http://"+thisUrl;
                    
//                    System.out.println("this url "+thisUrl);
                    try{
                        
                        response = Jsoup.connect(webUrl).followRedirects(true).execute();//exxcute url and get final url
                        excutedUrl = response.url().toString();
                    }   
                    catch (UnknownHostException error) {
                        System.err.println("Unknown host");
                        error.printStackTrace(); // I'd rather (re)throw it though.
                    }
                    if (excutedUrl.startsWith(redURL)&&linkDb.isInPool(excutedUrl)==false){//filter out the url that is not subpage after redirect

                            linkDb.addToDb(excutedUrl);
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
        linkDb = new LinksDb();
        
        ArrayList<String> emails = new ArrayList<String>();
        findEmail(emails,args[0]);
//        static 
       // findEmail(emails,"jana.com/contact");
        linkDb.printDb();
        System.out.print(emails);
    }


}
   