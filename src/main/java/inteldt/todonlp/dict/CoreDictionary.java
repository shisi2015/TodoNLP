package inteldt.todonlp.dict;

import static inteldt.todonlp.manager.Predefine.logger;
import inteldt.todonlp.manager.Config;
import inteldt.todonlp.manager.Predefine;
import inteldt.todonlp.model.Nature;
import inteldt.todonlp.seg.model.TrieAttribute;
import inteldt.todonlp.trie.TernaryTrie;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;

/**
 * 核心词典：存储词、词性的词典
 * 
 * @author lenovo
 *
 */
public class CoreDictionary {
	/**
	 * Trie树，词典的组织结构
	 */
	public static TernaryTrie<TrieAttribute> trie = new TernaryTrie<TrieAttribute>();
	/**
	 * 词典路径，设置为final，不可更改
	 */
	public final static String path = Config.CoreDictionaryPath;
	
	// 加载词典
    static
    {
    	logger.info("开始加载核心词典：" + path);
    	long start = System.currentTimeMillis();
        load(path);  
        logger.info("核心词典加载成功:" + trie.size() + "个词条，耗时" + (System.currentTimeMillis() - start) + "ms");
    }
   
    /**
     * 实际的加载词典
     * @param path  词典路径
     * @return
     */
    public static void load(String path){
    	if(loadDat(path + Predefine.BIN_EXT)) return;// 首先读取序列化词典，成功直接返回。
    	try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			String line;
			while((line = br.readLine()) != null){
				String[] params = line.split("\\s");// 分割
				int natureCount = (params.length - 1) / 2;// 词性的个数
				TrieAttribute attribute = new TrieAttribute(natureCount);
                for (int i = 0; i < natureCount; ++i)
                {
                    attribute.natures[i] = Enum.valueOf(Nature.class, params[1 + 2 * i]);
                    attribute.freqs[i] = Integer.parseInt(params[2 + 2 * i]);
                    attribute.totalFreq += attribute.freqs[i];
                }
                trie.insert(params[0], attribute);// 插入到词典中，构建词典
			} 
		} catch (FileNotFoundException e) {
			logger.warning("核心词典" + path + "不存在！" + e);
			System.exit(-1);
		} catch (IOException e) {
			logger.warning("核心词典" + path + "读取错误！" + e);
			System.exit(-1);
		}
    	
    	// 将词典序列化到磁盘上，以方便再次加载时 ，提高速度
    	logger.info("开始核心词典到" + path + Predefine.BIN_EXT);
    	try {
			ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(path + Predefine.BIN_EXT));
			output.writeObject(trie);
			output.close();
		} catch (Exception e) {
			logger.log(Level.WARNING, "在缓存核心词典" + path + Predefine.BIN_EXT + "时发生异常", e);
		} 
    }
    
    @SuppressWarnings("unchecked")
	public static boolean loadDat(String path){
    	try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(""));
			trie = (TernaryTrie<TrieAttribute>)in.readObject();
			in.close();
		} catch (Exception e) {
			logger.warning("读取失败，问题发生在" + e);
			return false;
		} 
    	
    	return true;
    }
}
