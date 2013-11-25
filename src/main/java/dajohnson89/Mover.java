package dajohnson89;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Mover {
    private static final String BASE_URL = "http://www.crunchyroll.com/tech-challenge/roaming-math/dajohnson89@gmail.com";
    private static final String GOAL = "GOAL";
    private static final String DEADEND = "DEADEND";


    private Graph<Page, Link> graph;
    Set<Link> encounteredLinks = new HashSet<>();
    Set<Page> encounteredPages = new HashSet<>();

    public Graph<Page, Link> explore(URL url) {
        traverse(url, null);
        Graph<Page, Link> graph = new Graph<>(encounteredLinks, encounteredPages);
        return graph;
    }

    private void traverse(URL url, Page parent) {
        String path = url.getPath();
        Long sourceID = Long.parseLong(path.substring(path.lastIndexOf('/') + 1));

        List<String> entries = getEntriesFromURL(url);
        //todo[dj] be a bit more careful here?
        if (entries.contains(GOAL) || entries.contains(DEADEND)) {
            Page endPage = handleSpecialPage(entries, sourceID);
            encounteredPages.add(endPage);
        } else {
            Page page = new Page(sourceID);
            for (String entry : entries) {
                Long destinationID = AntennaeUtils.evaluateExpression(entry);
                Link pageLink = new Link(sourceID, destinationID);
                page.getOutgoingList().add(pageLink);
                page.setParent(parent);
                if (!encounteredLinks.add(pageLink)) {
                    //debugging
                    System.out.println("cycle encountered. Not doing anything.");
                } else {
                    URL newURL = null;
                    String rawString = BASE_URL + '/' + destinationID;
                    try {
                        newURL = new URL(rawString);
                    } catch (MalformedURLException e) {
                        System.out.println("Malformed URL. [sourceID, destinationID] = ["+ sourceID + " " + destinationID+']');
                        e.printStackTrace();
                    }
                    traverse(newURL, page);
                }
            }
            encounteredPages.add(page);
        }
    }

    public void setGraph(Graph<Page, Link> graph) {
        this.graph = graph;
    }

    public Graph<Page, Link> getGraph() {
        return graph;
    }

    //todo[DJ]: Make the try/catch block a try with resources?
    private final List<String> getEntriesFromURL(URL url) {
        final List<String> entries = new ArrayList<>();
        BufferedReader br = null;
        try  {
            br = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = br.readLine()) != null) {
                // Process each line.
                entries.add(line);
            }
        } catch(IOException e) {
            System.out.println("Error reading from URL: " + url.toString());
        }
        return entries;
    }

    private final Page handleSpecialPage(List<String> entries, Long sourceID) {
        Page page = new Page(sourceID);
        if(entries.contains(GOAL)) {
            System.out.println("Goal reached at # " + sourceID);
            page.setIsGoal(true);
        } else {
            page.setIsDeadEnd(true);
        }
        return page;
    }



}
