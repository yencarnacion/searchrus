package abogadosScrapper

int countOccurences(char c, String s){
    int counter = 0;
    for( int i=0; i<s.length(); i++ ) {
        if( s.charAt(i) == c ) {
            counter++;
        }
    }
    return counter
}

def count=0, MAXSIZE=100
StringBuffer newlineBuffer = new StringBuffer()
Boolean start = true
new File("/Users/yamir/Dropbox/Documents/code/projects/abogame/notes/abogadosDePuertoRico.csv").withReader { reader ->
    def currentLine
    while ((currentLine = reader.readLine()) != null) {
        if(count++==0){
            println currentLine
            continue;
        }
        //def splitResult = currentLine.split(",")

        if (countOccurences(',' as char, currentLine)==6){
            if(!start){
                println newlineBuffer
                newlineBuffer = new StringBuffer()
            } else {
                start = false
            }
            newlineBuffer.append(currentLine.replaceAll("\n","").replaceAll("\r",""))
            continue;
        } else {
            newlineBuffer.append(currentLine.replaceAll("\n","").replaceAll("\r",""))

            continue;
        }
    }
    println newlineBuffer
}