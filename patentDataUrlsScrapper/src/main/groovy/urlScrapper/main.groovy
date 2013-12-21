import geb.Browser
 
Browser.drive {
    go "http://www.google.com/googlebooks/uspto-patents-grants.html"
 

    def links = $("a");
    int counter = 1;
    links.each { link ->
        println counter + "," +link.text() + "," + link.attr("href")
        counter ++
    }
}
