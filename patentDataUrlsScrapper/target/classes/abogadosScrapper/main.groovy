import geb.Browser
import geb.Module
import geb.Page


class Lawyer {
    String name;
    String category;
    String number;
    String recordLocator;
    String email;
    String notificationsAddress;
}

class LawyersPage extends Page {
    static content = {
        resultsCount { $('span#lblDirectoryResult tr').size()-1 }

        pagesCount { $('#divDirectoryTopPageIndex').find('option').text().toString().split(/\s/)[3].substring(1,4) }

        lawyersResult { index ->
            module LawyerRow, $("span#lblDirectoryResult tr", index + 1) // +1 is used to avoid header row
        }
    }
}

class LawyerRow extends Module {
    static content = {
        cell { i -> $("td", i) }
        name { cell(0).text() }
        category { cell(1).text() }
        idOrReference {
            def r = cell(2).find("a")
            if(r){
                r
            } else {
                cell(2).text()
            }
        }
    }
}

class LawyerDetailPage extends Page {
    static content = {
        lawyerDetailResultName { index ->
            module LawyerDetailRowData, $("#lblSearchResultDetail tr td", text: contains(~/Nombre Completo/)).parent("tr") // +1 is used to avoid header row
        }
        lawyerDetailResultNumber { index ->
            module LawyerDetailRowData, $("#lblSearchResultDetail tr td", text: contains(~/Tribunal Supremo/)).parent("tr") // +1 is used to avoid header row
        }
        lawyerDetailResultCategory { index ->
            module LawyerDetailRowData, $("#lblSearchResultDetail tr td", text: contains(~/Categor/)).parent("tr") // +1 is used to avoid header row
        }
        lawyerDetailResultEmail { index ->
            module LawyerDetailRowData, $("#lblSearchResultDetail tr td", text: contains(~/Email/)).parent("tr") // +1 is used to avoid header row
        }
        lawyerDetailResultNotificationsAddress { index ->
            module LawyerDetailRowData, $("#lblSearchResultDetail tr td", text: contains(~/de Notificaciones/)).parent("tr") // +1 is used to avoid header row
        }
    }
}

class LawyerDetailRowData extends Module {
    static content = {
        cell { i -> $("td", i) }
        data { cell(1).text() }
    }
}

def createLawyersCsv(String resourcesFolder,  String lawyersFileName, def lawyers){
    def lawyersCsv = new File("${resourcesFolder}/${lawyersFileName}")
    lawyersCsv.newWriter()
    lawyersCsv << ("number,name,category,email,notifications_address,record_locator") << "\r\n"
    lawyers.each { lawyer ->
        lawyersCsv << (lawyer.number?lawyer.number+",":",")
        lawyersCsv << (lawyer.name?lawyer.name+",":",")
        lawyersCsv << (lawyer.category?lawyer.category+",":",")
        lawyersCsv << (lawyer.email?lawyer.email+",":",")
        lawyersCsv << (lawyer.notificationsAddress?lawyer.notificationsAddress+",":",")
        lawyersCsv << (lawyer.recordLocator?lawyer.recordLocator:"") << "\r\n"
    }

}

def lawyers = new ArrayList();
def g_pagesCount=1;
Browser.drive() {

    setBaseUrl("https://unired.ramajudicial.pr/dirabogados/search.aspx")
    to LawyersPage
        for(int i=0; i<resultsCount; i++){
            def isReference = lawyersResult(i).idOrReference instanceof geb.content.SimplePageContent
            def recordLocator = null
            def number
            if(isReference){
                recordLocator = lawyersResult(i).idOrReference.attr('href')
                number = lawyersResult(i).idOrReference.text()
            } else {
                number = lawyersResult(i).idOrReference
            }
            def lawyer = new Lawyer(
                name: lawyersResult(i).name,
                category: lawyersResult(i).category,
                number: number,
                recordLocator: recordLocator
            )
            lawyers << lawyer
        }

        g_pagesCount = new Integer(pagesCount);
//    println resultsCount
//    println pagesCount
//    println lawyers;
    //Hay 65 en la primera pagina
    //for(lawyer in lawyersResult){
    //    println lawyer.name +" | "+ lawyer.category +" | "+lawyer.number
    //}
}

println "Scrapped Page 1"

for(int pageNumber=2; pageNumber<=g_pagesCount; pageNumber++){
    Browser.drive() {
        setBaseUrl("https://unired.ramajudicial.pr/dirabogados/Search.aspx?&i="+pageNumber)
        to LawyersPage
        for(int i=0; i<resultsCount; i++){
            def isReference = lawyersResult(i).idOrReference instanceof geb.content.SimplePageContent
            def recordLocator = null
            def number
            if(isReference){
                recordLocator = lawyersResult(i).idOrReference.attr('href')
                number = lawyersResult(i).idOrReference.text()
            } else {
                number = lawyersResult(i).idOrReference
            }
            def lawyer = new Lawyer(
                    name: lawyersResult(i).name,
                    category: lawyersResult(i).category,
                    number: number,
                    recordLocator: recordLocator
            )
            lawyers << lawyer
        }
        println "Scrapped Page "+pageNumber
    }
}

println "Processing Lawyers ["+lawyers.size()+"]"
int i = 1
for (def lawyer:lawyers){
    Browser.drive() {
        if(lawyer.recordLocator){
            setBaseUrl(lawyer.recordLocator)
            to LawyerDetailPage

            lawyer.email = lawyerDetailResultEmail.data
            lawyer.notificationsAddress = lawyerDetailResultNotificationsAddress.data
        }
    }
    println "Processed Lawyer ${i} of ["+lawyers.size()+"]"
    i++;

}

println "Creating Lawyers File in /tmp/abogadorDePuertoRico.csv"
createLawyersCsv("/tmp", "abogadorDePuertoRico.csv", lawyers)


