
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Text2Graph {
  private DirectedGraph graph;

  /**
   * javadoc注释内容
   * 
   * @since 1.0
   * @version 1.1
   * @author xxx
   * @throws UnsupportedEncodingException
   */
  public void init() throws FileNotFoundException,
      UnsupportedEncodingException {
    InputStream inputStreamsss = new FileInputStream(
        new File(Configuration.TextFilePath));
    InputStreamReader buf = new InputStreamReader(
        inputStreamsss, "utf-8");
    Scanner input = new Scanner(buf);

    String text = new String("");
    while (input.hasNext()) {
      String line = input.nextLine();
      text += line + " ";
    }

    input.close();

    /* 如果匹配到的第一个字符是非英文字符，将第一个单词前的内容全部抛弃 */
    Pattern p = Pattern.compile("[a-zA-Z]+");
    Matcher m = p.matcher(text);
    if (m.find()) {
      int firstLetterPos = m.start();
      if (firstLetterPos > 0) {
        text = text.substring(firstLetterPos);
      }
    }

    text = text.toLowerCase();

    String[] words = text.split("[^a-zA-Z]+");

    graph = generateGraph(words);
  }

  public String generateNewText(String inputText) {
    return generateNewText(graph, inputText);
  }

  /*
   * 根据桥接词生成新文本 根据之前生成的有向图，计算传入的文本中两两相邻的单词的桥接词，将桥接词插入到这两个相邻的单词之间。
   */
  /**
   * javadoc注释内容
   * 
   * @since 1.0
   * @version 1.1
   * @author xxx
   */
  public static String generateNewText(DirectedGraph graph,
      String inputText) {
    String[] words = inputText.split("[^a-zA-Z]+");
    String newText = "";
    int index = 0;

    if (words.length == 0) {
      return inputText;
    }

    for (int i = 0; i < words.length - 1; i++) {
      ArrayList<String> bridgeWords = queryBridgeWords(
          graph, words[i], words[i + 1]);
      String bridgeWord = "";
      if (bridgeWords.size() != 0) {
        Random r = new Random();
        index = r.nextInt(bridgeWords.size());
        bridgeWord = bridgeWords.get(index) + " ";
      }

      newText += words[i] + " " + bridgeWord;
    }
    newText += words[words.length - 1];

    return newText;
  }

  public String randomWalk() {
    return randomWalk(graph);
  }

  /*
   * 随机游走 随机的从图中选择一个节点，以此为起点沿出边进行随机遍历，记录经过的所有节点和边
   * 直到出现第一条重复的边为止，或者进入的某个节点不存在出边为止。利用遍历的所有节点来构造文本并返回。
   */
  /**
   * javadoc注释内容
   * 
   * @since 1.0
   * @version 1.1
   * @author xxx
   */
  public static String randomWalk(DirectedGraph graph) {
    String result = "";
    Random r = new Random();
    int vertIndex = r.nextInt(graph.vexList.length);

    DirectedGraph.VertexNode vvNode = graph.vexList[vertIndex];
    result += vvNode.myData;
    while (vvNode.myCount != 0) {
      DirectedGraph.EdgeNode eeNode = vvNode.myFirstEdge;
      int edgeIndex = r.nextInt(vvNode.myCount + 1);
      for (int i = 1; i < edgeIndex; i++) {
        eeNode = eeNode.myNext;
      }
      vertIndex = eeNode.myVertex;
      result += " " + graph.vexList[vertIndex].myData;
      if (eeNode.myVisited) {
        break;
      } else {
        eeNode.myVisited = true;
      }
      vvNode = graph.vexList[vertIndex];
    }

    graph.resetVisited();
    return result;
  }

  /*
   * 利用英文文本序列对应列表生成有向图 参数： 英文文本以空格为分割符分割所产生的单词列表 原理： 使用 wordsHashMap
   * 去除重复单词后存在字符串数组 filteredWords 中，作为顶点数据集合，保持单词在原文本中出现的先后次序。
   * 以单词为键，以单词在filteredWords 中的下标为值，存回wordsHashMap，以便生成边时使用。
   * 将每两个相邻单词连接为词组，以该词组为键，以该词组在文本中出现的次数为值，建立 phraseHashMap。 遍历
   * phraseHashMap，以词组的首个单词在顶点集合中的下标作为边的 src，以第二个单词的下标作为边的 dest
   * 以词组出现的次数作为边的weight，建立边集合。 利用顶点集合以及边集合建立有向图并返回。
   */
  /**
   * javadoc注释内容
   * 
   * @since 1.0
   * @version 1.1
   * @author xxx
   */
  public static DirectedGraph generateGraph(
      String[] words) {
    HashMap wordsHashMap = new HashMap();

    // 去除重复单词，以单词为键，单词出现的次序为值，存入 HashMap 中
    int count = 0;
    for (String word : words) {
      if (!wordsHashMap.containsKey(word)) {
        wordsHashMap.put(word, count);
        count++;
      }
    }

    String[] filteredWords = new String[wordsHashMap
        .size()];

    // 遍历 HashMap 的键，即不重复的单词集合，以单词对应的值作为下标，将单词存入新的字符串数组中
    Iterator iterator = wordsHashMap.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, Integer> entry = (Entry<String, Integer>) iterator
          .next();
      int index = entry.getValue();
      filteredWords[index] = entry.getKey();
    }

    // 将每两个相邻单词连接为词组，以该词组为键，以该词组在文本中出现的次数为值，建立 phraseHashMap。
    HashMap phraseHashMap = new HashMap();
    for (int i = 0; i < words.length - 1; i++) {
      String phrase = words[i] + " " + words[i + 1];
      if (phraseHashMap.containsKey(phrase)) {
        int value = (int) phraseHashMap.get(phrase);
        phraseHashMap.put(phrase, value + 1);
      } else {
        phraseHashMap.put(phrase, 1);
      }
    }

    Edge[] edges = new Edge[phraseHashMap.size()];
    int index = 0;
    Set<String> phraseKeys = phraseHashMap.keySet();
    for (String key : phraseKeys) {
      String[] splitPhrase = key.split(" ");
      int src = (int) wordsHashMap.get(splitPhrase[0]);
      int dest = (int) wordsHashMap.get(splitPhrase[1]);
      edges[index] = new Edge(src, dest,
          (int) phraseHashMap.get(key));
      index++;
    }

    return new DirectedGraph(filteredWords, edges);
  }

  public void outputGraph() {
    outputGraph(graph);
  }

  /*
   * 根据有向图生成 JPG 图片 通过遍历有向图邻接表来生成 dot 脚本，再调用 dot 程序来生成 JPG 图片。
   */
  /**
   * javadoc注释内容
   * 
   * @since 1.0
   * @version 1.1
   * @author xxx
   */
  public static void outputGraph(DirectedGraph graph) {
    String dot = "digraph G {\n";
    for (DirectedGraph.VertexNode vvNode : graph.vexList) {
      DirectedGraph.EdgeNode eeNode = vvNode.myFirstEdge;
      String from = vvNode.myData + "->";
      while (eeNode != null) {
        String color = "";
        if (eeNode.myVisited) {
          color = ", color=red";
        }
        dot += "\t" + from
            + graph.vexList[eeNode.myVertex].myData
            + " [label=" + eeNode.myWeight + color + "];\n";
        eeNode = eeNode.myNext;
      }
    }
    dot += "}";

    try {
      PrintWriter output = new PrintWriter(
          Configuration.DotScriptPath);
      output.write(dot, 0, dot.length());
      output.close();
      Process proc = Runtime.getRuntime().exec(
          new String[] { Configuration.GraphvizDotPath,
              Configuration.DotScriptPath, "-Tjpg", "-o",
              Configuration.JpgImagePath });
      proc.waitFor();
    } catch (IOException e) {
      // TODO 自动生成的 catch 块
      e.printStackTrace();
    } catch (InterruptedException e) {
      // TODO 自动生成的 catch 块
      e.printStackTrace();
    }

    graph.resetVisited();
  }

  /**
   * javadoc注释内容
   * 
   * @since 1.0
   * @version 1.1
   * @author xxx
   */
  @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
  public String queryBridgeWords(String word1,
      String word2) {
    ArrayList<String> bridgeWords = queryBridgeWords(graph,
        word1, word2);
    String result = "";
    final String toString = " to ";

    if (bridgeWords.size() == 0) {
      int indexOfWord1 = graph.getIndexOfWord(word1);
      int indexOfWord2 = graph.getIndexOfWord(word2);
      if (indexOfWord1 == -1 || indexOfWord2 == -1) {
        result = "No " + word1 + " or " + word2
            + " in the graph!";
      } else {
        result = "No bridge words from " + word1 + toString
            + word2 + "!";
      }
      return result;
    }

    result = "The bridge words from " + word1 + toString
        + word2 + " are:";
    int count = 1;
    int size = bridgeWords.size();
    for (String word : bridgeWords) {

      if (count == 1) {
        result += word;
      } else if (count == size) {
        result += " and " + word + ".";
      } else {
        result += ", " + word;
      }
      count++;
    }

    return result;
  }

  /*
   * 查询桥接词 根据参数 word1和 word2，查询 graph中是否存在这两个单词 若存在。 并且 graph中存在 word1->word3
   * word3->word2 这两条边，则 word3 为桥接词，将其添加到 ArrayList 中 否则返回空 Array
   */
  /**
   * javadoc注释内容
   * 
   * @since 1.0
   * @version 1.1
   * @author xxx
   */
  public static ArrayList<String> queryBridgeWords(
      DirectedGraph graph, String word1, String word2) {
    int indexOfWord1 = graph.getIndexOfWord(word1);
    int indexOfWord2 = graph.getIndexOfWord(word2);

    ArrayList<String> bridgeWords = new ArrayList<String>();

    if (indexOfWord1 == -1 || indexOfWord2 == -1) {
      return bridgeWords;
    }

    DirectedGraph.EdgeNode nodeNextToWord1 = graph.vexList[indexOfWord1].myFirstEdge;
    while (nodeNextToWord1 != null) {
      DirectedGraph.EdgeNode eeNode = graph.vexList[nodeNextToWord1.myVertex].myFirstEdge;
      while (eeNode != null) {
        String adjWord = graph.vexList[eeNode.myVertex].myData;
        if (adjWord.equals(word2)) {
          String bridgeWord = graph.vexList[nodeNextToWord1.myVertex].myData;
          bridgeWords.add(bridgeWord);
        }
        eeNode = eeNode.myNext;
      }
      nodeNextToWord1 = nodeNextToWord1.myNext;
    }

    return bridgeWords;
  }

  /*
   * 计算两个顶点间的距离 如果两个顶点之间存在边，则返回边的权值，否则返回无穷大
   */
  /**
   * javadoc注释内容
   * 
   * @since 1.0
   * @version 1.1
   * @author xxx
   */
  public static int calcDist(DirectedGraph graph, int src,
      int dest) {
    DirectedGraph.EdgeNode nodeNextToWord1 = graph.vexList[src].myFirstEdge;
    while (nodeNextToWord1 != null) {
      if (dest == nodeNextToWord1.myVertex) {
        return nodeNextToWord1.myWeight;
      } else {
        nodeNextToWord1 = nodeNextToWord1.myNext;
      }
    }
    return Integer.MAX_VALUE;
  }

  /*
   * 计算最短路径的prev数组 参数：待查询的单词需确保在图中出现
   */
  /**
   * javadoc注释内容
   * 
   * @since 1.0
   * @version 1.1
   * @author xxx
   */
  public static int[] calcPrevArray(DirectedGraph graph,
      String word1) {
    final int maxInt = Integer.MAX_VALUE;
    int vertNum = graph.vexList.length;
    int[] dist = new int[vertNum];
    int[] prev = new int[vertNum];
    boolean[] ssS = new boolean[vertNum];
    int src = graph.getIndexOfWord(word1);

    ssS[src] = true;

    // 初始化当前最短距离
    for (int i = 0; i < vertNum; i++) {
      dist[i] = calcDist(graph, src, i);
    }

    // 初始化最短路径前驱顶点
    for (int i = 0; i < vertNum; i++) {
      if (dist[i] == maxInt) {
        prev[i] = -1;
      } else {
        prev[i] = src;
      }
    }

    for (int i = 0; i < vertNum; i++) {
      if (i == src) {
        continue;
      }

      int minDist = maxInt;
      int v = src;

      // 在未标记的顶点中，找到距离源点最近的顶点，并进行标记
      for (int j = 0; j < vertNum; j++) {
        if (!ssS[j] && dist[j] < minDist) {
          v = j;
          minDist = dist[j];
        }
      }
      ssS[v] = true;

      // 以刚标记完的顶点为中心，更新其邻接点与源点当前的最短距离
      for (int j = 0; j < vertNum; j++) {
        int tempDist = calcDist(graph, v, j);
        if (!ssS[j] && tempDist < maxInt) {
          if (dist[v] + tempDist < dist[j]) {
            dist[j] = dist[v] + tempDist;
            prev[j] = v;
          }
        }

        // 判断当前顶点是否存在到达源点的边，若存在且有需要，则更新源点到达自身的 dist 和 prev
        if (j == src && calcDist(graph, v, src) < maxInt
            && dist[v] + tempDist < dist[src]) {
          dist[src] = dist[v] + tempDist;
          prev[src] = v;
        }
      }
    }

    return prev;
  }

  /**
   * javadoc注释内容
   * 
   * @since 1.0
   * @version 1.1
   * @author xxx
   */
  public String calcShortestPath(String word1,
      String word2) {
    if (word2.equals("")) {
      return calcShortestPath(graph, word1);
    } else {
      return calcShortestPath(graph, word1, word2);
    }
  }

  /*
   * 计算两个单词之间的最短路径 利用 Dijkstra 算法计算非负带权有向图的最短路径，处理了源点存在路径回到自身的情况
   */
  /**
   * javadoc注释内容
   * 
   * @since 1.0
   * @version 1.1
   * @author xxx
   */
  public static String calcShortestPath(DirectedGraph graph,
      String word1, String word2) {
    int src = graph.getIndexOfWord(word1);
    int dest = graph.getIndexOfWord(word2);

    if (src == -1 || dest == -1) {
      return "No path from " + word1 + " to " + word2 + ".";
    }

    int[] prev = calcPrevArray(graph, word1);

    // 不存在 word1 到 word2 的路径 （包含不存在自身到自身路径的情况）
    if ((dest == src && prev[src] == -1)
        || prev[dest] == -1) {
      return "No path from " + word1 + " to " + word2 + ".";
    }

    // 计算word1到Word2的最短路径
    String result = "";
    int length = 0;
    int curIndex = dest;

    // 形成最短路径字符串并计算长度
    do {
      // 将最短路径对应的邻接表的边设为已访问
      DirectedGraph.EdgeNode eeNode = graph.vexList[prev[curIndex]].myFirstEdge;
      while (eeNode.myVertex != curIndex) {
        eeNode = eeNode.myNext;
      }
      eeNode.myVisited = true;
      length += eeNode.myWeight;

      result = "->" + graph.vexList[curIndex].myData
          + result;
      curIndex = prev[curIndex];
    } while (curIndex != src);
    result = graph.vexList[src].myData + result;
    result = "The length of shortest path is: "
        + String.valueOf(length) + "\n" + result;

    return result;
  }

  /*
   * 计算源点到其他可达节点的最短路径并返回
   */
  /**
   * javadoc注释内容
   * 
   * @since 1.0
   * @version 1.1
   * @author xxx
   */
  public static String calcShortestPath(DirectedGraph graph,
      String word) {
    int src = graph.getIndexOfWord(word);
    if (src == -1) {
      return "No " + word + " in the graph!";
    }

    int[] prev = calcPrevArray(graph, word);
    String result = "";

    for (int dest = 0; dest < graph.vexList.length; dest++) {
      if ((dest == src && prev[src] == -1)
          || prev[dest] == -1) {
        continue;
      }

      String path = "";
      int curIndex = dest;

      // 形成最短路径字符串
      do {
        path = "->" + graph.vexList[curIndex].myData + path;
        curIndex = prev[curIndex];
      } while (curIndex != src);
      path = graph.vexList[src].myData + path;
      result += (path + "\n");
    }
    return result.trim();
  }

}
