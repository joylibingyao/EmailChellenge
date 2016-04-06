import java.util.ArrayList;

/**
 *
 * @author bingyaoli
 */
public class LinksDb {
    private ArrayList<String> linksDb;

    public LinksDb() {
        this.linksDb = new ArrayList<String>();
    }
    
    public boolean isInPool(String url){

            for(String linkData: linksDb){
                    if(linkData.equals(url)){
                        return true;
                    }
            }

        return false;
    }

    public ArrayList<String> getLinksDb() {
        return linksDb;
    }
    
    public void printDb(){
        System.out.println("Link in DB: "+linksDb);
    }
    public void addToDb(String url){
        this.linksDb.add(url);
    }
}
