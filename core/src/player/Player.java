package player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import map.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class Player {

    HashMap<Integer, HashMap<Integer, Integer>> mapScore;
    HashMap<Integer, HashMap<Integer, Integer>> mapStars;
    private int stars;

    public Player() {
        mapScore = new HashMap<>();
        mapStars = new HashMap<>();
        loadPlayer();
    }

    public void update(Map map) {
        int oldScore;
        int oldStars;
        if (mapScore.get(map.getMapWorld()) == null) {
            mapScore.put(map.getMapWorld(), new HashMap<Integer, Integer>());
            mapStars.put(map.getMapWorld(), new HashMap<Integer, Integer>());
        }

        if (mapScore.get(map.getMapWorld()).get(map.getMapNumber()) != null)
            oldScore = mapScore.get(map.getMapWorld()).get(map.getMapNumber());
        else
            oldScore = 100;
        if (mapScore.get(map.getMapWorld()).get(map.getMapNumber()) != null)
            oldStars = mapStars.get(map.getMapWorld()).get(map.getMapNumber());
        else
            oldStars = 0;
        int newScore = map.getMovesTaken();
        int newStars = 0;

        if (map.getGoals().size() == 3) {
            if (map.getMovesTaken() <= map.getGoals().get(2))
                newStars = 4;
            else if (map.getMovesTaken() <= map.getGoals().get(1))
                newStars = 3;
            else if (map.getMovesTaken() <= map.getGoals().get(0))
                newStars = 2;
            else newStars = 1;
        } else if (map.getGoals().size() == 2) {
            if (map.getMovesTaken() <= map.getGoals().get(1))
                newStars = 3;
            else if (map.getMovesTaken() <= map.getGoals().get(0))
                newStars = 2;
            else newStars = 1;
        } else if (map.getGoals().size() == 1) {
            if (map.getMovesTaken() <= map.getGoals().get(0))
                newStars = 2;
            else newStars = 1;
        } else if (map.getGoals().size() == 0) {
            newStars = 1;
        }

        if (newScore < oldScore) {
            mapScore.get(map.getMapWorld()).put(map.getMapNumber(), newScore);
        }
        if (newStars > oldStars) {
            mapStars.get(map.getMapWorld()).put(map.getMapNumber(), newStars);
            stars = stars + (newStars - oldStars);
        }

    }

    public void loadPlayer() {


        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {


            dBuilder = builderFactory.newDocumentBuilder();
            Document document;
            FileHandle file = Gdx.files.local("player.xml");
            if (file.exists()) {
                document = dBuilder.parse(file.read());
            } else {
                document = dBuilder.parse(Map.class.getResourceAsStream("/resources/newPlayer.xml"));
            }

            document.normalize();

            NodeList rootNodes = document.getElementsByTagName("player");
            Node rootNode = rootNodes.item(0);
            Element player = (Element) rootNode;

            NodeList stars = player.getElementsByTagName("stars");
            Element starsEle = (Element) stars.item(0);
            String starCount = starsEle.getAttribute("count");
            this.stars = Integer.parseInt(starCount);

            NodeList worlds = player.getElementsByTagName("world");
            for (int worldNum = 0; worldNum < worlds.getLength(); worldNum++) {
                Element worldEle = (Element) worlds.item(worldNum);
                int WorldNumber = Integer.parseInt(worldEle.getAttribute("nr"));
                this.mapScore.put(WorldNumber, new HashMap<Integer, Integer>());
                this.mapStars.put(WorldNumber, new HashMap<Integer, Integer>());

                NodeList maps = worldEle.getElementsByTagName("map");
                for (int mapNum = 0; mapNum < maps.getLength(); mapNum++) {
                    Element mapEle = (Element) maps.item(mapNum);
                    int mapNumber = Integer.parseInt(mapEle.getAttribute("nr"));
                    int mapScore = Integer.parseInt(mapEle.getAttribute("score"));
                    int mapStars = Integer.parseInt(mapEle.getAttribute("stars"));

                    this.mapScore.get(WorldNumber).put(mapNumber, mapScore);
                    this.mapStars.get(WorldNumber).put(mapNumber, mapStars);

                }
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

    }

    public void savePlayer() {

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {

            dBuilder = builderFactory.newDocumentBuilder();
            Document document = dBuilder.newDocument();

            Element player = document.createElement("player");
            document.appendChild(player);

            Element stars = document.createElement("stars");
            stars.setAttribute("count", Integer.toString(this.stars));
            player.appendChild(stars);

            for (Integer world : mapScore.keySet()) {
                Element worldEle = document.createElement("world");
                worldEle.setAttribute("nr", world.toString());
                player.appendChild(worldEle);
                for (Integer mapNr : mapScore.get(world).keySet()) {
                    Element mapEle = document.createElement("map");
                    mapEle.setAttribute("nr", mapNr.toString());
                    mapEle.setAttribute("score", mapScore.get(world).get(mapNr).toString());
                    mapEle.setAttribute("stars", mapStars.get(world).get(mapNr).toString());
                    worldEle.appendChild(mapEle);

                }
            }

            document.normalize();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Source xmlSource = new DOMSource(document);
            Result outputTarget = new StreamResult(outputStream);
            TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
            InputStream is = new ByteArrayInputStream(outputStream.toByteArray());

            FileHandle file = Gdx.files.local("player.xml");
            file.write(is, false);


        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }


    }

    public int getStarsFromLevel(int world, int level) {
        int stars;

        if (mapStars.get(world) == null) {
            stars = 0;
        } else if (mapStars.get(world).get(level) == null) {
            stars = 0;
        } else {
            stars = mapStars.get(world).get(level);
        }
        return stars;


    }

    public int getStars() {
        return stars;
    }


}