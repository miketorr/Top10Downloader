//package empty.top10downloader;
//
//import android.util.Log;
//
//import org.xmlpull.v1.XmlPullParser;
//
//import static android.content.ContentValues.TAG;
//
//public class dummy {
//
//    while (eventType != XmlPullParser.END_DOCUMENT) {                                       //we get those events and respond to them in our code, WHILE the end of the document isn't reached.
//        String tagName = xpp.getName();                                                     //get the name of the current tag
//
//        switch (eventType) {                                                                //take different actions depending on the type of events happening inside the parser
//            case XmlPullParser.START_TAG:                                                   //at some point will read the start of a tag in the xml and when that happens the eventtype will change to START_TAG
//                //Every time there is a START tag event and the tag is entry then we get ready to store the data for our application
//                Log.d(TAG, "parse: Starting tag for " + tagName);
//                if ("entry".equalsIgnoreCase(tagName)) {                                    //if that (above) happens we are only interested if its an <entry> tag in the xml
//                    inEntry = true;
//                    currentRecord = new FeedEntry();                                        //create a new FeedEntry instance ready to start putting the data into.
//                }
//                break;
//
//            case XmlPullParser.TEXT:                                                        //if the event type is text, the pullparser is telling us that data is available so we store it in the textValue variable below
//                textValue = xpp.getText();                                                  //store the text wherever a new text is available, but doesn't do anything with it until the END TAG event starts below. The text here is the text inside the entries
//                //of the xml. The entries are <name/>, <artist/> and so on
//                break;
//
//            case XmlPullParser.END_TAG:                                                     //When the xmlpullparser finds the end tag, any code in xml with />, <\, like in <im:name> Pokemon GO <\im:name> the code inside this case will
//                Log.d(TAG, "parse: Ending tag for" + tagName);
//                if (inEntry) {                                                              //see which tag we are talking about
//                    if ("entry".equalsIgnoreCase(tagName)) {                                //if the entry tag is equal to our tagName, meaning if the entry tag is <entry> and the tagName we got at the start of the method is entry
//                        applications.add(currentRecord);                                    //the array list applications will have added the currentRecord value, which will be entry and the inentry variable will be false
//                        inEntry = false;
//                    } else if ("name".equalsIgnoreCase(tagName)) {                          //if the entry is <name/> or something like it, in this example will be <\im:name>, compare the string with tagName, that will be change to name
//                        currentRecord.setName(textValue);                                   //and set the value inside this tag 'name', that is stored as a TEXT entry (the code already passed through the case TEXT) to the array list,
//                        // so the array will store the value inside the tag name and so on for the rest here
//                    } else if ("artist".equalsIgnoreCase(tagName)) {
//                        currentRecord.setArtist(textValue);
//                    } else if ("releaseDate".equalsIgnoreCase(tagName)) {
//                        currentRecord.setReleaseDate(textValue);
//                    } else if ("summary".equalsIgnoreCase(tagName)) {
//                        currentRecord.setSummary(textValue);
//                    } else if ("image".equalsIgnoreCase(tagName)) {
//                        currentRecord.setImageURL(textValue);
//                    }
//                }
//                break;
//}
//
//
//
