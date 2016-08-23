package com.renke;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.renke.api.ChannelHandler;
import com.renke.constant.HTTPConstant;
import com.renke.exception.HTTPException;
import com.renke.exception.HandlerException;
import com.renke.util.StringHex;


public class HTTPChannelHandler implements ChannelHandler {
	private final static Logger logger = LoggerFactory.getLogger(HTTPChannelHandler.class);
	private static Lock lock = new ReentrantLock();
	private static HTTPChannelHandler HTTPhandler;
	/**
	 * ����ģʽ
	 * @author renke.zuo@foxmail.com
	 * @time 2016-07-29 15:13:13
	 * @return
	 */
	public static HTTPChannelHandler getHTTPChannelHandler(){
		if(HTTPhandler == null){
			lock.lock();
			if(HTTPhandler == null){
				HTTPhandler = new HTTPChannelHandler();
			}
			lock.unlock();
		}
		return HTTPhandler;
	}
	
	private HTTPChannelHandler(){
	}
	
	/**
	 * ����HTTP request����
	 * @author renke.zuo@foxmail.com
	 * @date 2016��7��29��
	 * @param socketChannel
	 * @return map
	 * @throws Exception
	 */
	@Override
	public Map<String,Object> resolveSocketChannel(SocketChannel socketChannel) throws Exception {
		long begin = System.currentTimeMillis();
		logger.debug("localAddress:{}��remoteAddress:{}",socketChannel.getLocalAddress(),socketChannel.getRemoteAddress());
		ByteBuffer request = ByteBuffer.allocate(1024);
		byte[] tmp = new byte[1024];
		//�ܳ���
		int countLen = 0;
		//��ͷ����
		int beginIndex = 0;
		//��ȡ���ݳ���
		int len = 0;
		//�����Ƿ����
		boolean hasSpace = false;
		//��ʱ������ݶ���
		List<byte[]> tmpList = new ArrayList<byte[]>();
		Map<String,Object> result = new HashMap<String,Object>();
		//����������װ�뵽ByteBuffer��
		while((len = socketChannel.read(request)) > 0){
			//��ȡ��byteBuffer�У��Ὣposition����Ϊlen+1
			//���ԣ������ȡ��������Ҫ����ByteBuffer�еļ�������
			//��limit����Ϊposition����position����Ϊ0����mark����Ϊ-1
			//���ڶ�ȡ������ǣ���0��ʼ����ȡlimit������
			request.flip();
			System.arraycopy(request.array(),0, tmp,beginIndex,len);
			//ʣ���ܳ���
			countLen = beginIndex + len;
			beginIndex = 0;
			//readLine����������ʱ����ǿ�������
			for(int i=0;i<countLen;i++){
				//����֮ǰ��http�����ͷ��Ϣ
				if(i < countLen - 1 && tmp[i]=='\r' && tmp[i+1]=='\n' && !hasSpace){
					byte[] lineBytes = new byte[i-beginIndex];
					if(lineBytes.length == 0){
						hasSpace = true;
					}else{
						System.arraycopy(tmp, beginIndex, lineBytes, 0, lineBytes.length);
					}
					tmpList.add(lineBytes);
					++i;
					beginIndex = i+1;
					if(i < countLen - 2 && tmp[i+1] =='\r' && tmp[i+2]=='\n'){
						tmpList.add(new byte[0]);
						hasSpace = true;
						++i;++i;
						beginIndex = i+1;
					}
				}else if(hasSpace){
					//����֮�󣬾���post�ύ���������������У���ȡ���ٱ�����٣�ÿ��һ��listԪ��
					byte[] lineBytes = new byte[countLen - beginIndex];
					System.arraycopy(tmp, beginIndex, lineBytes, 0, countLen - beginIndex);
					tmpList.add(lineBytes);
					beginIndex = 0;
					break;
				}
			}
			//ͷ��Ϣ���д洢
			//ʣ�����ݱ���������ͷ��
			if(beginIndex < countLen && !hasSpace) {
				System.arraycopy(tmp, beginIndex, tmp,0,countLen-beginIndex);
			}
			/**
			 * FIXME ��Ҫ�ж�request����Ч�ԡ�
			 */
			if(hasSpace){
				//������ϣ���ֱ�ӽ���
				if(resolveRequstByBytes(tmpList,result)) break;
			}
			
		}
		request = null;
		tmp = null;
		tmpList = null;
		socketChannel.shutdownInput();
		logger.debug("resolveRequest Time:{} ms",System.currentTimeMillis()-begin);
		return result;
	}
	
	/**
	 * ������T��װ��response������
	 * @author renke.zuo@foxmail.com
	 * @date 2016��7��29��
	 * @param t
	 * @param socketChannel
	 * @return
	 * @throws Exception
	 */
	@Override
	public byte[] assembleData(byte[] data) throws Exception {
		String encoding = Controller.encoding;
		int length = data.length;
		byte[] response = getResponseHead(length,encoding);
		byte[] result = new byte[response.length + length];
		System.arraycopy(response, 0, result, 0, response.length);
		System.arraycopy(data, 0, result, response.length, length);
		return result;
//		socketChannel.write(ByteBuffer.wrap(respone.getBytes(encoding)));
//		socketChannel.write(ByteBuffer.wrap(result));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> byte[] handle(T t) throws HandlerException{
		if(!(t instanceof Map)) return null;
		String encoding = Controller.encoding;
		Map<String,Object> map = (Map<String, Object>) t;
		Iterator<String> it = map.keySet().iterator();
		StringBuilder sb = new StringBuilder();
		while(it.hasNext()){
			String key = it.next();
			Object obj = map.get(key);
			if(obj instanceof String){
				sb.append(key).append("=[").append(obj).append("];");
			}
			if(obj instanceof LinkedNodeQueue){
				LinkedNode[] lna = ( (LinkedNodeQueue) obj).toArray();
				sb.append(key).append("=[");
				for(int i=0;i<lna.length;i++){
					LinkedNode ln = lna[i];
					if(i<lna.length-1)
						sb.append(ln.getData()).append(",");
					else
						sb.append(ln.getData());
				}
				sb.append("];");
			}
		}
		String result = sb.toString();
		//����request
//		String result = CollectionUtil.mapToString(map);
		logger.info("result: \r\n{}",result);
		try {
			return result.getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			throw new HandlerException(e);
		}
	}
	
	private void resolveRequestHead(byte[] tmp,Map<String,Object> map){
		for(int index = 0; index<tmp.length ; index++){
			byte b = tmp[index] ;
			if(b==':'){
				index ++;
				map.put(new String(tmp,0,index - 1).trim(), new String(tmp,index,tmp.length - index).trim());
				break;
			}
			if(index == tmp.length - 1){
				map.put(HTTPConstant.FIRST_LINE, tmp);
			}
		}
	}
	
	private void resolveRequestData(Map<String,Object> map) throws UnsupportedEncodingException{
		byte[] tmp = (byte[])map.get(HTTPConstant.POST_DATA);
		if(tmp == null) return;
		resolveData(tmp,map);
	}
	
	private void resolveData(byte[] data,Map<String,Object> map) throws UnsupportedEncodingException{
		String encoding = Controller.encoding;
		byte[] tmp = new byte[data.length];
		int index = 0;
		String param_name = "";
		String param_value = "";
		LinkedNodeQueue lnq ;
		//��������
		for(int i=0;i<data.length;i++){
			if(data[i]=='%'){
				tmp[index++] = StringHex.decode(Arrays.copyOfRange(data, i+1, i+3));
				i++;i++;
			}else if(data[i]=='='){
				param_name = new String(tmp,0,index,encoding);
				index = 0;
			}else if(data[i] == '&'){
				param_value = new String(tmp,0,index,encoding);
				lnq = (LinkedNodeQueue)map.get(param_name);
				LinkedNode ln = new LinkedNode();
				ln.setData(param_value);
				if(lnq == null){
					lnq = new LinkedNodeQueue();
				}
				lnq.add(ln);
				map.put(param_name, lnq);
				index = 0;
			}else if(data[i]=='+'){
				tmp[index++] = ' ';
			}else{
				tmp[index++] = data[i];
			}
		}
		if(index > 0){
			param_value = new String(tmp,0,index,encoding);
			lnq = (LinkedNodeQueue)map.get(param_name);
			LinkedNode ln = new LinkedNode();
			ln.setData(param_value);
			if(lnq == null){
				lnq = new LinkedNodeQueue();
			}
			lnq.add(ln);
			map.put(param_name, lnq);
		}
		try {
			logger.info("data:{}",new String(tmp,0,index,encoding));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * ����HTTP request���ĵ�һ��
	 * ��ȡmethod url?param httpVersion
	 * @author renke.zuo@foxmail.com
	 * @time 2016-07-29 15:13:55
	 * @param map
	 * @throws HTTPException
	 * @throws UnsupportedEncodingException 
	 */
	private void resolveRequestFirstLine(Map<String,Object> map) throws HTTPException, UnsupportedEncodingException{
		byte[] tmp = (byte[])map.get(HTTPConstant.FIRST_LINE);
		if(tmp == null) throw new HTTPException("http request is wrong!");
		int u_index = 0;
		int h_index = 0;
		int p_index = 0;
		for(int i=0;i<tmp.length;i++){
			if(tmp[i] == ' '){
				if(u_index == 0){
					u_index = i+1;
					map.put(HTTPConstant.METHOD, new String(tmp,0,i));
				}else if(h_index ==0){
					h_index = i+1;
					//����data
					resolveData(Arrays.copyOfRange(tmp, p_index, i),map);
				}
			}
			if(tmp[i] == '?'){
				map.put(HTTPConstant.URL, new String(tmp,u_index,i-u_index));
				p_index = i+1;
			}
		}
		map.put(HTTPConstant.HTTP_VERSION, new String(tmp,h_index,tmp.length-h_index));
	}
	
	/**
	 * ����HTTP request����
	 * �����Ƿ�������
	 * @author renke.zuo@foxmail.com
	 * @time 2016-07-29 11:39:41
	 * @param tmpList
	 * @param result
	 * @return
	 * @throws HTTPException 
	 * @throws UnsupportedEncodingException 
	 */
	private boolean resolveRequstByBytes(List<byte[]> tmpList,Map<String,Object> result) throws HTTPException, UnsupportedEncodingException{
		boolean spaceOver = false;
		boolean resolveHead = false;
		//�ж�head�Ƿ��Ѿ��������
		if(result.get(HTTPConstant.HEAD_OVER) != null) resolveHead = true;
		
		//ȡ�����ܳ���
		Object tmp = result.get(HTTPConstant.CONTENT_LENGTH);
		int contentLength = tmp == null? 0: (int)tmp;
		
		//��ȡ�Ѷ����ݳ���
		tmp = result.get(HTTPConstant.DATA_SIZE);
		int dataSize = tmp == null? 0: (int)tmp;
		
		//��ȡ�Ѷ�����
		tmp = result.get(HTTPConstant.POST_DATA);
		byte[] data = tmp==null?null:(byte[])tmp;
		
		for(int i=0;i<tmpList.size();i++){
			byte[] tmpb = tmpList.get(i);
			//��װHTTPData����
			if(spaceOver){
				if(contentLength < dataSize) break;
				System.arraycopy(tmpb, 0, data, dataSize,
							Math.max(contentLength-dataSize,tmpb.length));
				dataSize += tmpb.length;
			}else{
				if(tmpb.length == 0){
					spaceOver = true;
					resolveHead = true;
				}else if(!resolveHead){//��װHTTPRequestͷ��Ϣ
					resolveRequestHead(tmpb,result);
				}
				//��ʼ��
				if(spaceOver && resolveHead){
					//���屨��ͷ���������
					result.put(HTTPConstant.HEAD_OVER, true);
					//��ȡcontentLength
					Object content_length = result.get(HTTPConstant.CONTENT_LENGTH);
					if(content_length != null){
						contentLength = Integer.parseInt((String)content_length);
						result.put(HTTPConstant.CONTENT_LENGTH, contentLength);
					}
					//��ʼ����Ҫ��ȡ����������
					data = new byte[contentLength];
				}
			}
		}
		result.put(HTTPConstant.POST_DATA, data);
		//�����Ѷ����ݳ��ȣ�ͬʱ���ؽ��
		if(dataSize >= contentLength){
			result.put(HTTPConstant.DATA_SIZE, contentLength);
			resolveRequestFirstLine(result);
			resolveRequestData(result);
			return true;
		}else{
			result.put(HTTPConstant.DATA_SIZE, dataSize);
			return false;
		}
	}
	
	/**
	 * ��ȡ����ͷ
	 * 
	 * @author renke.zuo@foxmail.com
	 * @time 2016-08-01 16:53:48
	 * @param length
	 * @param encoding
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private static byte[] getResponseHead(int length,String encoding) throws UnsupportedEncodingException{
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/1.1 200 OK\r\n")
		.append("Date: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis())).append(" GMT+8\r\n")
		.append("Content-Type: text/html;charset=").append(encoding).append("\r\n")
		.append("Content-Length:").append(length).append("\r\n")
		.append("\r\n");
		return sb.toString().getBytes(encoding);
	}
	
	/**
	 * ÿ���������
	 * ��Ҫ�߱�  �����C�������S�����ݶ�M��չʾ��V
	 * ����һ��ѹ���˺ʹ���˺ϲ�����һ�𣬽��黹�Ƿֿ�
	 * ���ݶ˺�չʾ���ں�̨���ݽ���ʱ�����Է���һ�������Ҫ���û��������ֿ�����
	 * 
	 * �������������Ӧ����ô�ع�
	 * 
	 * 
	 */
}
