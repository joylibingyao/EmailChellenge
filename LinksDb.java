import java.util.ArrayList;
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
        System.out.println("All links in DB"+linksDb);
    }
    public void addToDb(String url){
        if(this.isInPool(url)==false){
            this.linksDb.add(url);
        }
        
    }
}
