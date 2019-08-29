package readmire.offloadinglibrary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.lang.CharSequence;
import android.app.Activity;
import android.content.Context;
import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import com.google.dexmaker.stock.ProxyBuilder;

/**
 *
 *    @yasemin
 **/

public class OffloadingFactory{
	public static String tagg = "  FOLLOW-ME  ";
	public static HashMap<Long,Object> proxyObjectContainer;
	public static HashMap<Long,Object> realObjectContainer;
	private static String serverIpAddress = "";
	//private static String serverIpAddress = "";
	protected static ControlParameters controlParameters;
	static long sendRequest, receiveRequest,receiveImageResponse, 
	            imageSendTime, networkOverhead , serverProcessTime;
    public static ProfilingManager profiler;
    public static ArrayList<Vertex> nodeTree;
	public static Context contextx;
	private static boolean connected = false;
	private Handler handler = new Handler();
	private static byte [] imgbyte;
	String filepath;
	static Context myContext;
	static ConstructorParam cpp;
	static CheckForClass cfc;
	static ServerResult serverResult;
	private Class<?>[] constructorArgTypes = new Class[0];
    private Object[] constructorArgValues = new Object[0];
    public static int COUNTER;
	
    private OffloadingFactory(){
					
	}
	private static OffloadingFactory singleton;
	
	public static OffloadingFactory getInstance() {
		if (singleton == null) {
			synchronized (OffloadingFactory.class) {
				if (singleton == null) {
					singleton = new OffloadingFactory();
				}
			}
		}
		return singleton;
	}
	// if Proxy objects need the constructor values, use these
	public OffloadingFactory constructorArgValues(Object... constructorArgValues) {
        this.constructorArgValues = constructorArgValues;
        return this;
    }

    public OffloadingFactory constructorArgTypes(Class<?>... constructorArgTypes) {
        this.constructorArgTypes = constructorArgTypes;
        return this;
    }
    
	public static void initvar(Activity act){
		if(profiler==null)
		   profiler = new ProfilingManager();
		if(cfc == null){
		   cfc = new CheckForClass();		   
		}
		cfc.initialize();		
	    if(proxyObjectContainer==null)
			proxyObjectContainer = new HashMap<Long,Object>();
	    if(realObjectContainer == null){
	    	realObjectContainer = new HashMap<Long, Object>();
	    }
//	    DecisionManager dm = new DecisionManager();
//	    dm.loadOffloadableArray(act);
	   
	}
	
	public static CheckForClass getCFC(){
		return cfc;
	}
	
	public static byte[] toByteArray(int value) {
	     return  ByteBuffer.allocate(4).putInt(value).array();
	}
	public static Context getApplicationContext(){
		return myContext;
	}
    public static <T> T create(Class<T> TypeTest, final Context context, final ConstructorParam cp ){
	     Class<T>  cls = null;
	     cpp=cp;
	     myContext=context;	  
	    try {	     
	    	if(!(cfc.checkOffloadble(TypeTest.getSimpleName()+"_Proxy"))&& !cfc.getOffClass().isEmpty()){
	    		if(cp==null){
	    			Object obj = TypeTest.newInstance();
	    			final Long oid = generateRandom();
	    			realObjectContainer.put(oid, obj);
	    			System.out.println("real object is created without cons: "+ obj.getClass().getName());
	    			return (T) obj;
	    		}else{
	    			Constructor constructor = TypeTest.getConstructor(cp.getConstructorArgTypes());
					Object ctobj = constructor.newInstance(cp.getConstructorArgValues());
					final Long oid = generateRandom();
	    			realObjectContainer.put(oid, ctobj);
	    			System.out.println("real object is created with cons: "+ ctobj.getClass().getName());
					return (T) ctobj;
	    		}
	    	}	    	
	    	
			final Long oid = generateRandom();			
			// want to create proxy invocation handler 		
			InvocationHandler handler = new InvocationHandler() {
					@Override					
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable { 
						String className = proxy.getClass().getCanonicalName();
						//System.out.println("class name: "+ proxy.getClass().getName());
						className = getClassNameFromDexMaker(className);
						String packageName = context.getPackageName();
						className = context.getPackageName() + "." + className;
						//System.out.println("class name :: "+proxy.getClass().getSimpleName());												
						//parameter control is handled and ids are found. primitives are sent directly.
						//Log.d(tagg,"Offloadable class&method: "+className+"->"+method.getName()); 
					 if(ControlParameters.isOffload()==true){					                  
						   try {
								Log.d(tagg, "in proxy method!!! "+method.getName());
								InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
							    Socket socket = new Socket(serverAddr, 9877);
							    socket.setTcpNoDelay(true);
							    Long objectOid = getKeyByValue(proxyObjectContainer, proxy);  //getIdFromObject(proxy);
			    	    		Long newOid = null;  		    	    		
			    	    		if(objectOid!=null){
			    	    			newOid =objectOid; 
			    	    			System.out.println("get id of object created before");
			    	    		}else{
			    	    			newOid = oid;
			    	    		} 
							    Object[] changedArgs = checkMethodParameters(args,context);
								//socket.setSoTimeout(10000);
							    //if(method.getName().equals("hashCode")){
							    	if(cpp != null){							    	
							    	  Object[] changedConsParam = checkMethodParameters(cp.getConstructorArgValues(),context);
							    	  cp.setConstructorArgValues(changedConsParam);
							    	  serverResult = new ServerResult(newOid, className, method.getName(), changedArgs, cp);
							    	  
							    	}else{
							    	  serverResult = new ServerResult(newOid, className, method.getName(), changedArgs, null);
							    	}
//							    }else{
//							    	  serverResult = new ServerResult(newOid, className, method.getName(), changedArgs, null);	
//							    }
							    OutputStream os = socket.getOutputStream();	
								ObjectOutputStream oos = new ObjectOutputStream(os); 
								oos.writeObject(serverResult); 
								
								InputStream is = socket.getInputStream();  
								ObjectInputStream ois = new ObjectInputStream(is); 
								
								Log.d("ClientActivity", "object is sent to cloudlet.");
								oos.flush();							 
	                            	                            
	                            Object obj = (Object)ois.readObject();	                            
	                            //Log.d("ClientActivity", "C: Closed.");
	                            //ois.close();
	                            //oos.close();
	                            //socket.close();
	                            cpp=null;
	                            return obj;
						            
						      } catch (Exception e) {
						            Log.e("ClientActivity", "Error: Read-write or Object server error", e);
						            e.printStackTrace();						           
						      }	
					       
					    } //end of offload

					    long sizes = calculateArgumentSize(args);			        
				        Object result=null;
				        // method call begin
				        long start = System.currentTimeMillis();				       
					    result = ProxyBuilder.callSuper(proxy, method, args);
						long elapsed = System.currentTimeMillis()-start;						
//					    System.out.println("Method: " + method.getName()+ " args: " + 
//						Arrays.toString(args)+ " result: " + result + " -- args size : "+ 
//					    		String.valueOf(sizes)+ " -- Elapsed time: "+elapsed);
                        long rsizes = 0;
                        if(result != null) {
                            rsizes = calculateReturnValueSize(result);
                            sizes = sizes + rsizes;
                        }
					    Throwable t = new Throwable(); 
						StackTraceElement[] elements = t.getStackTrace();
						if(!method.getName().equals(toString()))
						   profiler.startProfiling(elements, method, args, elapsed, sizes, rsizes, packageName);

				   return result;						
				}					
		   };            
		   Object proxyObject;
		   if(cp==null){			   
			   //System.out.println("no constructor arguments");
			   proxyObject = ProxyBuilder.forClass(TypeTest)
						.dexCache(context.getDir("dx", Context.MODE_PRIVATE))
						.handler(handler).build();
		   }else {		   
		       proxyObject = ProxyBuilder.forClass(TypeTest)
					.constructorArgValues(cp.getConstructorArgValues())
					.constructorArgTypes(cp.getConstructorArgTypes())
					.dexCache(context.getDir("dx", Context.MODE_PRIVATE))
					.handler(handler).build();
		   }		  
		   proxyObjectContainer.put(oid, proxyObject);
		   return (T)proxyObject;
		
		} catch (Exception e) {
			e.printStackTrace();
			
			return null;
		}		
	
   }  // end of object creation
   
   public static long calculateArgumentSize(Object[] args){
	   long sizes = 0;
       for(int i = 0; i < args.length; i++){
          if(args[i] instanceof Bitmap){
       	   byte[] by = getBytesFromBitmap((Bitmap)args[i]);
       	   sizes += by.length;
          }else if(args[i] instanceof byte[]){				        	  
       	   long t = ((byte[]) args[i]).length;
       	   System.out.println("byte array size: "+ t);
       	   sizes += t;				        	    
          }else if(args[i].getClass().isArray()){				        	   
   		   sizes += findArraySize(args[i]);  		          
          }else if(args[i] instanceof Collection){
   		   sizes += (((Collection<?>)args[i]).size())*8; 
          }else if(args[i] instanceof Proxy){
       	   sizes += 8; 
          }else if(args[i].getClass().isPrimitive()){
       	   sizes += 8 ;  
          }else if(args[i] instanceof Bitmap){
       	   long t = findObjectSize(args[i]);
       	   System.out.println("Compressed Image or FV size: "+t);
       	   sizes += t;
          }else{
       	   sizes += 8; 
          }
       }
       return sizes;
   }
   
   public static long calculateReturnValueSize(Object result){
	   long sizes = 0;       
          if(result instanceof Bitmap){
       	   byte[] by = getBytesFromBitmap((Bitmap)result);
       	   sizes += by.length;
          }else if(result instanceof byte[]){				        	  
       	   long t = ((byte[]) result).length;
       	   System.out.println("byte array size: "+ t);
       	   sizes += t;				        	    
          }else if(result.getClass().isArray()){				        	   
   		   sizes += findArraySize(result);  		          
          }else if(result instanceof Collection){
   		   sizes += (((Collection<?>)result).size())*8; 
          }else if(result instanceof Proxy){
       	   sizes += 8; 
          }else if(result.getClass().isPrimitive()){
       	   sizes += 8 ;  
          }else if(result instanceof Bitmap){
       	   long t = findObjectSize(result);
       	   System.out.println("Compressed Image or FV size: "+t);
       	   sizes += t;
          }else{
       	   sizes += 8; 
          }
    
       return sizes;
   }
    
   public static Object getServerResult(ServerResult sr,Activity act) {		
		Object result = null;
		Object[] chArgs = new Object[sr.getArgs().length];
		// if there is the same object on the server, does not create, get the id
		if (proxyObjectContainer.containsKey(sr.getObjectID())|| realObjectContainer.containsKey(sr.getObjectID())){			
			System.out.println("gets object id created before");
			System.out.println("number of parameters: "+ sr.getArgs().length);
					Class partypes[] = new Class[sr.getArgs().length];
					Object[] args = sr.getArgs();						
					for (int i = 0; i < args.length; i++) {							
						if(args[i] instanceof ParametersObjectTypes){
							ParametersObjectTypes pot = (ParametersObjectTypes) args[i];
							if(proxyObjectContainer.containsKey(pot.getId())){
								Object pobj = proxyObjectContainer.get(pot.getId());
								partypes[i] = pobj.getClass();
								chArgs[i]=pobj;								
								System.out.println("pot olan proxy object: "+ pobj.getClass().getName());
							}else if(realObjectContainer.containsKey(pot.getId())){
								Object robj = realObjectContainer.get(pot.getId());
								partypes[i] = robj.getClass();
								chArgs[i]=robj;								
								System.out.println("pot - real object: "+ robj.getClass().getName());
							}else if(pot.getType().equals("Activity")||pot.getType().equals("interdroid.eyedentify.EyeDentify")){
								chArgs[i] = getApplicationContext();
								partypes[i] = Context.class;
								System.out.println("Activity: " + chArgs[i]);
							}else{
								if(pot.isLocal()){
									Class<?> clazz = null;
									try {
										clazz = Class.forName(pot.getType());
									} catch (ClassNotFoundException e) {									
										e.printStackTrace();
									}
									Object pobj = create2(clazz,act,null,pot.getId());
									chArgs[i] = pobj;
									partypes[i] = clazz;
									proxyObjectContainer.put(pot.getId(),chArgs[i]);								
									System.out.println("proxy object as param is created: "+clazz);
								}else{
									Class<?> clazz = null;
									Object robj = null;
									try {
										clazz = Class.forName(pot.getType());
										robj = clazz.newInstance();
									} catch (ClassNotFoundException e) {									
										e.printStackTrace();
									}catch (InstantiationException e) {
										e.printStackTrace();
									}catch (IllegalAccessException e) {
										e.printStackTrace();
									}
									
									chArgs[i] = robj;
									partypes[i] = clazz;
									realObjectContainer.put(pot.getId(),chArgs[i]);
									System.out.println("real object as param is created : "+clazz);
								}
								
							}	
							System.out.println("ParametersObjectTypes: " + chArgs[i].getClass().getName());						
						
						}else if(isWrapperType2(args[i].getClass())){							
							partypes[i] = getPrimitiveType(args[i].getClass());
							chArgs[i]=args[i];
							System.out.println("recieved param1: "+chArgs[i].getClass());
						}else {	
							chArgs[i]=args[i];
							partypes[i] = args[i].getClass();
							System.out.println("recieved param2: " +chArgs[i].getClass() );
						}											
					}
					
					try {
						Class<?> c = null;
						c = Class.forName(sr.getClassName());
						Object obj = null;
						if(proxyObjectContainer.containsKey(sr.getObjectID())){
							obj = proxyObjectContainer.get(sr.getObjectID());
						}else{
							obj = realObjectContainer.get(sr.getObjectID());
						}						
						Method method = null;
						if(sr.getMethodName().equals("hashCode")){
							method = c.getMethod(sr.getMethodName());
							result = method.invoke(obj);
						}else {
						    method = c.getDeclaredMethod(sr.getMethodName(),partypes);
						    result = method.invoke(obj, chArgs);
						}
						System.out.println("method invoke: "+sr.getClassName()+"->"+method.getName());
					} catch (NoSuchMethodException e) {						
						e.printStackTrace();
					} catch (ClassNotFoundException e) {						
						e.printStackTrace();
					} catch (IllegalArgumentException e) {						
						e.printStackTrace();
					} catch (IllegalAccessException e) {						
						e.printStackTrace();
					} catch (InvocationTargetException e) {						
						e.printStackTrace();
					}
					
				return result;
			
		} else {
			try {
				//object is created first time and it has a constructor param
				if(sr.getCp()!= null){
					System.out.println("there is not any object created before and has constructor");
					ConstructorParam cp = new ConstructorParam();					
					Object[] ctargs = sr.getCp().getConstructorArgValues();
					Class ctpartypes[] = new Class[sr.getCp().getConstructorArgTypes().length];
					for (int i = 0; i < ctargs.length; i++) {					
						if(ctargs[i] instanceof ParametersObjectTypes){
							ParametersObjectTypes pot = (ParametersObjectTypes) ctargs[i];
							if(proxyObjectContainer.containsKey(pot.getId())){
								Object obj = proxyObjectContainer.get(pot.getId());								
								ctargs[i]=obj;
								ctpartypes[i] = obj.getClass();
								System.out.println("proxy constructor param: "+ctargs[i].getClass().getName());
								
							}else if(realObjectContainer.containsKey(pot.getId())){
								Object robj = realObjectContainer.get(pot.getId());
								ctargs[i]=robj;
								ctpartypes[i] = robj.getClass();																
								System.out.println("pot - real object: "+ robj.getClass().getName());
							}else if(pot.getType().equals("Activity")||pot.getType().equals("interdroid.eyedentify.EyeDentify")){
								ctargs[i] = getApplicationContext();
								ctpartypes[i] = Context.class;
								System.out.println("Activity: " + ctargs[i].getClass().getName());
							}else{
								if(pot.isLocal()){
									Class<?> clazz = null;
									try {
										clazz = Class.forName(pot.getType());
									} catch (ClassNotFoundException e) {									
										e.printStackTrace();
									}
									Object pobj = create2(clazz,act,null,pot.getId());
									ctargs[i] = pobj;
									ctpartypes[i] = clazz;
									proxyObjectContainer.put(pot.getId(),ctargs[i]);								
									System.out.println("proxy object as param is created: "+clazz);
								}else{
									Class<?> clazz = null;
									Object robj=null;
									try {
										clazz = Class.forName(pot.getType());
										robj = clazz.newInstance();
									} catch (ClassNotFoundException e) {									
										e.printStackTrace();
									}									
									ctargs[i] = robj;
									ctpartypes[i] = clazz;
									realObjectContainer.put(pot.getId(),ctargs[i]);
									System.out.println("real object as param is created: "+clazz);
								}
															
							}
							System.out.println("ParametersObjectTypes: " + ctargs[i].getClass().getName());
						}else if(isWrapperType2(ctargs[i].getClass())){							
							ctpartypes[i] = getPrimitiveType(ctargs[i].getClass());
							System.out.println("received param1: "+ctargs[i].getClass());
						}else{
							ctpartypes[i] = ctargs[i].getClass();
							System.out.println("received param2: "+ctargs[i].getClass());
						}	
					}
					
					Class partypes[] = new Class[sr.getArgs().length];
					Object[] args = sr.getArgs();
					for (int i = 0; i < sr.getArgs().length; i++) {
						if(args[i] instanceof ParametersObjectTypes){
							ParametersObjectTypes pot = (ParametersObjectTypes) args[i];
							if(proxyObjectContainer.containsKey(pot.getId())){
								Object obj = proxyObjectContainer.get(pot.getId());
								chArgs[i]=obj;
								partypes[i] = obj.getClass();
								System.out.println("proxy  param: "+obj.getClass().getName());
							}else if(realObjectContainer.containsKey(pot.getId())){
								Object robj = realObjectContainer.get(pot.getId());
								chArgs[i]=robj;
								partypes[i] = robj.getClass();																
								System.out.println("proxy  param: "+robj.getClass().getName());
							}else if(pot.getType().equals("Activity")||pot.getType().equals("interdroid.eyedentify.EyeDentify")){
								chArgs[i] = getApplicationContext();
								partypes[i] = Context.class;
								System.out.println("Activity : " + chArgs[i].getClass().getName());
							}else{
								
								if(pot.isLocal()){
									Class<?> clazz = null;
									try {
										clazz = Class.forName(pot.getType());
									} catch (ClassNotFoundException e) {									
										e.printStackTrace();
									}
									Object pobj = create2(clazz,act,null,pot.getId());
									chArgs[i] = pobj;
									partypes[i] = clazz;
									proxyObjectContainer.put(pot.getId(),chArgs[i]);								
									System.out.println("proxy object as param is created : "+clazz);
								}else{
									Class<?> clazz = null;
									try {
										clazz = Class.forName(pot.getType());
									} catch (ClassNotFoundException e) {									
										e.printStackTrace();
									}
									Object robj = clazz.newInstance();
									chArgs[i] = robj;
									partypes[i] = clazz;
									realObjectContainer.put(pot.getId(),chArgs[i]);
									System.out.println("real object as param is created: "+clazz);
								}
								
							}
							System.out.println("ParametersObjectTypes" + chArgs[i].getClass().getName());						
							
						}else if(isWrapperType2(args[i].getClass())) {							
							partypes[i] = getPrimitiveType(args[i].getClass());
							chArgs[i]=args[i];
						}else{
							partypes[i] =sr.getArgs()[i].getClass();
							chArgs[i] = sr.getArgs()[i];
						}					
						System.out.println("Parameter Type - "+ (i+1) +" : "+ partypes[i].getName());
					}
					
					Class<?> ctClass = Class.forName(sr.getClassName());					
					// creating an object by getting Constructor object (with parameters) and calling newInstance (with parameters) on it
					Constructor constructor = ctClass.getConstructor(ctpartypes);
					Object ctObject = constructor.newInstance(ctargs);
					Method method = null;
					if(sr.getMethodName().equals("hashCode")){
						method = ctClass.getMethod(sr.getMethodName());
						result = method.invoke(ctObject);
					}else {
					    method = ctClass.getDeclaredMethod(sr.getMethodName(),partypes);
					    result = method.invoke(ctObject, chArgs);
					}					
					realObjectContainer.put(sr.getObjectID(), ctObject);					
				    return result;
				}else{
				//object created for first time and no constructor
				System.out.println("there is not any object created before, without constructor");
				Class partypes[] = new Class[sr.getArgs().length];
				Object[] args = sr.getArgs();
				for (int i = 0; i < sr.getArgs().length; i++) {
					if(args[i] instanceof ParametersObjectTypes){
						ParametersObjectTypes pot = (ParametersObjectTypes) args[i];
						if(proxyObjectContainer.containsKey(pot.getId())){
							Object obj = proxyObjectContainer.get(pot.getId());
							chArgs[i]=obj;
							partypes[i] = obj.getClass();
							System.out.println("proxy  param: "+obj.getClass().getName());
						}else if(realObjectContainer.containsKey(pot.getId())){
							Object robj = realObjectContainer.get(pot.getId());
							chArgs[i]=robj;
							partypes[i] = robj.getClass();																
							System.out.println("proxy  param: "+robj.getClass().getName());
						}else if(pot.getType().equals("Activity")||pot.getType().equals("interdroid.eyedentify.EyeDentify")){
							chArgs[i] = getApplicationContext();
							partypes[i] = Context.class;
							System.out.println("Activity : " + chArgs[i].getClass().getName());
						}else{
							
							if(pot.isLocal()){
								Class<?> clazz = null;
								try {
									clazz = Class.forName(pot.getType());
								} catch (ClassNotFoundException e) {									
									e.printStackTrace();
								}
								Object pobj = create2(clazz,act,null,pot.getId());
								chArgs[i] = pobj;
								partypes[i] = clazz;
								proxyObjectContainer.put(pot.getId(),chArgs[i]);								
								System.out.println("proxy object as param is created: "+clazz);
							}else{
								Class<?> clazz = null;
								try {
									clazz = Class.forName(pot.getType());
								} catch (ClassNotFoundException e) {									
									e.printStackTrace();
								}
								Object robj = clazz.newInstance();
								chArgs[i] = robj;
								partypes[i] = clazz;
								realObjectContainer.put(pot.getId(),chArgs[i]);
								System.out.println("real object as param is created: "+clazz);
							}
							
						}
						System.out.println("ParametersObjectTypes" + chArgs[i].getClass().getName());						
						
					}else if(isWrapperType2(args[i].getClass())) {							
						partypes[i] = getPrimitiveType(args[i].getClass());
						chArgs[i]=args[i];
					}else{
						partypes[i] =args[i].getClass();
						chArgs[i] = args[i];
					}					
					System.out.println("Parameter Type - "+ (i+1) +" : "+ partypes[i].getName());
				}
				Class<?> c = null;
				c = Class.forName(sr.getClassName());
				Object obj = c.newInstance();
				Method method = null;
				if(sr.getMethodName().equals("hashCode")){
					method = c.getMethod(sr.getMethodName());
					result = method.invoke(obj);
				}else {
				    method = c.getDeclaredMethod(sr.getMethodName(),partypes);
				    result = method.invoke(obj, chArgs);
				}
				realObjectContainer.put(sr.getObjectID(), obj);
				return result;
			  }	
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}
	}
   
  public static Object[] checkConstructorParameters(Object[] args, Context context){
		System.out.println("Number of Constructor Parameters : "+ args.length);
		ArrayList<Object> AllTypes = new ArrayList<Object>();
		for (int i = 0; i < args.length; i++) {	
			System.out.println(args[i].getClass().getSimpleName()+" : "+ProxyBuilder.isProxyClass(args[i].getClass()));
			if (isWrapperType2(args[i].getClass())) {                
				 AllTypes.add(args[i]);
			}else if (ProxyBuilder.isProxyClass(args[i].getClass())){ 
              // if parameter is proxy, get ids from container
			   Long id = getKeyByValue(proxyObjectContainer,args[i]);
               ParametersObjectTypes pot = new ParametersObjectTypes();
               pot.setId(id);
               String className = args[i].getClass().getCanonicalName();
			   className = getClassNameFromDexMaker(className);
			   String packageName = context.getPackageName();				
               pot.setType(className);
               pot.setLocal(false);
               AllTypes.add(pot);
               System.out.println("param check proxy object: " + className);

			}else if(realObjectContainer.containsValue(args[i])){
				Long id = getKeyByValue(realObjectContainer, args[i]);
				ParametersObjectTypes pot = new ParametersObjectTypes();
                pot.setId(id);
                String className = args[i].getClass().getName();
                pot.setType(className);
                pot.setLocal(true);
                AllTypes.add(pot);
				System.out.println("param check real object: "+className);
			}else if(args[i] instanceof Activity){
				ParametersObjectTypes pot = new ParametersObjectTypes();
				pot.setId(12345L);
				pot.setType("Activity");			
				System.out.println(args[i].getClass().getSimpleName());
				AllTypes.add(pot);
			}else{
				AllTypes.add(args[i]);
			}
		
		}				
		return AllTypes.toArray();
	}
   
	public static Object[] checkMethodParameters(Object[] args, Context context){
		System.out.println("Number of Method Parameters: "+ args.length);
		ArrayList<Object> AllTypes = new ArrayList<Object>();
		for (int i = 0; i < args.length; i++) {	
			//System.out.println(args[i].getClass().getSimpleName()+" : "+ProxyBuilder.isProxyClass(args[i].getClass()));
			if (isWrapperType2(args[i].getClass())) {                
				 AllTypes.add(args[i]);
				 System.out.println("check method param: " + args[i].getClass());
			}else if (ProxyBuilder.isProxyClass(args[i].getClass())){ 
                // if parameter is proxy, get ids from container
				Long id = getKeyByValue(proxyObjectContainer,args[i]);
                ParametersObjectTypes pot = new ParametersObjectTypes();
                pot.setId(id);
                String className = args[i].getClass().getName();
				className = getClassNameFromDexMaker(className);
				String packageName = context.getPackageName();
				className = packageName + "." + className;
				pot.setLocal(false);
                pot.setType(className);                
                AllTypes.add(pot);
                System.out.println("check method proxy object: "+ className);
			}else if(realObjectContainer.containsValue(args[i])){
				Long id = getKeyByValue(realObjectContainer, args[i]);
				ParametersObjectTypes pot = new ParametersObjectTypes();
                pot.setId(id);
                String className = args[i].getClass().getName();
                pot.setType(className);
                pot.setLocal(true);
                AllTypes.add(pot);
				System.out.println("check method real object: "+className);
			}else if(args[i] instanceof Activity){
				ParametersObjectTypes pot = new ParametersObjectTypes();
				pot.setId(12345L);
				pot.setType("Activity");			
				System.out.println("received param1: "+args[i].getClass().getSimpleName());
				AllTypes.add(pot);				
			}else{
				AllTypes.add(args[i]);
				System.out.println("received param2: " +args[i].getClass().getName() );
			}
		}				
		return AllTypes.toArray();
	}
    

    public static Class<?> getPrimitiveType(Class<?> clazz) {
    	if(clazz.equals(String.class)){
			return CharSequence.class;
        }else if(clazz.equals(Boolean.class)){
			return Boolean.TYPE;
		}else if(clazz.equals(Integer.class)){
			return Integer.TYPE;
		}else if(clazz.equals(Character.class)){
			return Character.TYPE;
		}else if(clazz.equals(Byte.class)){
			return Byte.TYPE;
		}else if(clazz.equals(Short.class)){
			return Short.TYPE;
		}else if(clazz.equals(Double.class)){
			return Double.TYPE;
		}else if(clazz.equals(Long.class)){
			return Long.TYPE;
		}else if(clazz.equals(Float.class)){
			return Float.TYPE;
		}else{
			return null;
		}	
	}
   
    public static int findObjectSize(Object o){    	
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
	    	oos.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
    	
    	 return baos.size();
    }
    // findObjectSize can be used instead of method below
	public static int findArraySize(Object obj){
		int a=1;
		int b=1;
		int c=1;
		int factor=1;
		if (obj instanceof Object[][] || obj instanceof boolean[][] ||
				obj instanceof byte[][] || obj instanceof short[][] ||
				obj instanceof char[][] || obj instanceof int[][] ||
				obj instanceof long[][] || obj instanceof float[][] ||
				obj instanceof double[][]){
			if(obj.getClass().getSimpleName().equals("int[][]")){
				int[][] x = (int[][])obj;
				a= Array.getLength(obj);
				if(a>0){
					b= x[a-1].length;
				}
				factor=4;
			}else if(obj.getClass().getSimpleName().equals("double[][]")){
				double[][] x = (double[][])obj;
				a= Array.getLength(obj);
				if(a>0){
					b= x[a-1].length;
				}
				factor=8;
			}
			return a*b*factor;
		}else if(obj instanceof Object[][][] || obj instanceof boolean[][][] ||
				obj instanceof byte[][][] || obj instanceof short[][][] ||
				obj instanceof char[][][] || obj instanceof int[][][] ||
				obj instanceof long[][][] || obj instanceof float[][][] ||
				obj instanceof double[][][]){
			if(obj.getClass().getSimpleName().equals("int[][][]")){
				int[][][] x = (int[][][])obj;
				a= Array.getLength(obj);
				if(a>0){
					b= x[a-1].length;
				}
				if(b>0){
					c=x[a-1][b-1].length;
				}
				factor = 4;
			}else if(obj.getClass().getSimpleName().equals("double[][][]")){
				double[][][] x = (double[][][])obj;
				a= Array.getLength(obj);
				if(a>0){
					b= x[a-1].length;
				}
				if(b>0){
					c=x[a-1][b-1].length;
				}
				factor = 8;
			}
			return a*b*c*8;
		}else{
			a= Array.getLength(obj);
			return a*8;
		}

	}
	public static int findObjectSizeInstrument(Object o){
    	Instrumentation instrumentation = new Instrumentation();
    	Bundle b = instrumentation.getAllocCounts();
    	return -1;    	
    }

    
    public static long getImageSendTime(){
    	
    	return imageSendTime;
    }
    
    public static long getNetworkOverhead(){
    	return networkOverhead;
    }
    
    public static long getServerProcessTime(){
    	return serverProcessTime;
    }
    
	public static String getClassNameFromDexMaker(String className){
		//_Proxy daha guvenli bir kontrol olur
		return className.substring(0,className.indexOf("_"));		
	}  
	
	public static long generateRandom() {

		int length = 12;
		Random random = new Random();
		char[] digits = new char[length];
		digits[0] = (char) (random.nextInt(9) + '1');
		for (int i = 1; i < length; i++) {
			digits[i] = (char) (random.nextInt(10) + '0');
		}
		long id = Long.parseLong(new String(digits));

		if (!proxyObjectContainer.containsKey(id)) {
			return id;
		} else {
			return generateRandom();
		}

	}
    
	 public static int fromByteArray(byte[] bytes) {
	     return ByteBuffer.wrap(bytes).getInt();
	}
	 
//	public static Long getIdFromObject(Object obj) {
//		Long id = null;
//		//id=ObjectContainer.get(obj);
//		for (Map.Entry<Object, Long> entry : ObjectContainer.entrySet()) {
//			if (entry.getKey()==obj) {
//				id = (Long) entry.getValue();
//				System.out.println("gonderilen object id  : " +id);
//			}
//
//		}
//		return id;
//	}
  public static <Long, Object> Long getKeyByValue(HashMap<Long, Object> hashmap, Object obj) {
		if(!hashmap.isEmpty()){
		    for (Entry<Long,Object> entry : hashmap.entrySet()) {
		        if (entry.getValue()== obj) {
		            return entry.getKey();
		        }
		    }
		}
	    return null;
	}
	
	public static <Long, Object> Object getValueByKey(HashMap<Long, Object> hashmap, Long id) {
		Object obj = null;
		obj = hashmap.get(id);
	    return obj;
	}
	
//	public static Object getProxyObjectFromId(Long id) {
//		Object obj = null;
//		obj = realObjectContainer.get(id);
////		for (Map.Entry<Long, Object> entry : proxyObjectContainer.entrySet()) {
////			if (entry.getKey().equals(id)) {
////				obj = entry.getValue();
////				
////			}
////		}
//		return obj;
//	}
//	
//	public static Long getRealObjectId(Object obj){
//		
//		Long id = null;
//		//id=(Long) realObjectContainer.get(obj);
//		for (Map.Entry<Long, Object> entry : proxyObjectContainer.entrySet()) {
//
//			if (entry.getValue().equals(obj)) {
//				id = (Long) entry.getKey();
//			}
//
//		}
//		return id;		
//		
//	}
	

	public static boolean isWrapperType2(Class<?> clazz) {
		return clazz.equals(Boolean.class) ||
			clazz.equals(Integer.class) ||
			clazz.equals(Character.class) ||
			clazz.equals(Byte.class) ||
			clazz.equals(Short.class) ||
			clazz.equals(Double.class) ||
			clazz.equals(Long.class) ||
			clazz.equals(Float.class);
	}

	public static byte[] getBytesFromBitmap(Bitmap bitmap) {		
	    ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    bitmap.compress(CompressFormat.PNG,100, stream);
	    return stream.toByteArray();
	    
	}
	
	
	public static <T> T create2(Class<T> TypeTest, final Context context, final ConstructorParam cp, final Long oid ){
	     Class<T>  cls = null;
	     cpp=cp;
	     myContext=context;	  
	    try {
			// want to create proxy invocation handler 		
			InvocationHandler handler = new InvocationHandler() {
					@Override					
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable { 
						String className = proxy.getClass().getCanonicalName();
						//System.out.println("class name: "+ proxy.getClass().getName());
						className = getClassNameFromDexMaker(className);
						String packageName = context.getPackageName();
						className = context.getPackageName() + "." + className;
						//System.out.println("class name :: "+proxy.getClass().getSimpleName());
						System.out.println("Offloadable class&method: "+className+"->"+method.getName());
						//parameter control : find object and primitive types
					 if(ControlParameters.isOffload()==true){					                  
						   try {
								//Log.d(tagg, "for proxy "+className+"->"+method.getName());
								//System.out.println("Offloadable class&method: "+className+"->"+method.getName());
								InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
							    Socket socket = new Socket(serverAddr, 9877);
							    Long objectOid = getKeyByValue(proxyObjectContainer, proxy);  //getIdFromObject(proxy);
			    	    		Long newOid = null;  		    	    		
			    	    		if(objectOid!=null){
			    	    			newOid =objectOid; 
			    	    		}else{
			    	    			newOid = oid;
			    	    		} 
							    Object[] changedArgs = checkMethodParameters(args,context);
								//socket.setSoTimeout(10000);
//							    if(method.getName().equals("hashCode")){
							    	if(cpp != null){							    	
							    	  Object[] changedConsParam = checkMethodParameters(cp.getConstructorArgValues(),context);
							    	  cp.setConstructorArgValues(changedConsParam);
							    	  serverResult = new ServerResult(newOid, className, method.getName(), changedArgs, cp);
							    	  
							    	}else{
							    	  serverResult = new ServerResult(newOid, className, method.getName(), changedArgs, null);
							    	}
//							    }else{
//							    	  serverResult = new ServerResult(newOid, className, method.getName(), changedArgs, null);	
//							    }
							    OutputStream os = socket.getOutputStream();	
								ObjectOutputStream oos = new ObjectOutputStream(os); 
								oos.writeObject(serverResult); 
								
								InputStream is = socket.getInputStream();  
								ObjectInputStream ois = new ObjectInputStream(is); 
								
								Log.d("ClientActivity", "object is sent to cloudlet.");
								oos.flush();							 
	                            //oos.close();	                            
	                            Object obj = (Object)ois.readObject();	                            
	                            //Log.d("ClientActivity", "C: Closed.");
	                            //ois.close();
	                            cpp = null;
	                            return obj;
								
						            
						      } catch (Exception e) {
						            Log.e("ClientActivity", "Error: Read-write or Object server return error", e);
						            e.printStackTrace();						           
						      }	
					       
					    } //end of offload
						long sizes = 0;
				        for(int i = 0; i < args.length; i++){
				           if(args[i] instanceof Bitmap){
				        	   byte[] by = getBytesFromBitmap((Bitmap)args[i]);
				        	   sizes += by.length;
				           }else if(args[i] instanceof byte[]){				        	  
				        	   long t = ((byte[]) args[i]).length;
				        	   System.out.println("byte array size: "+ t);
				        	   sizes += t;				        	    
				           }else if(args[i].getClass().isArray()){				        	   
			        		   sizes += findArraySize(args[i]);  		          
				           }else if(args[i] instanceof Collection){
			        		   sizes += (((Collection<?>)args[i]).size())*8; 
				           }else if(args[i] instanceof Proxy){
				        	   sizes += 8; 
				           }else if(args[i].getClass().isPrimitive()){
				        	   sizes += 8 ;  
				           }else if(args[i] instanceof Bitmap){
				        	   long t = findObjectSize(args[i]);
				        	   System.out.println("Compressed Image or FV size: "+t);
				        	   sizes += t;
				           }else{
				        	   sizes += 8; 
				           }
				        }				        
				        Object result=null;
				        // method call begin
				        long start = System.currentTimeMillis();				       
					      result = ProxyBuilder.callSuper(proxy, method, args);
						long elapsed = System.currentTimeMillis()-start;						
//					    System.out.println("Method: " + method.getName()+ " args: " + 
//						Arrays.toString(args)+ " result: " + result + " -- args size : "+ 
//					    		String.valueOf(sizes)+ " -- Elapsed time: "+elapsed);
                        long rsizes = 0;
                        if(result != null) {
                            rsizes = calculateReturnValueSize(result);
                            sizes = sizes + rsizes;
                        }
					    Throwable t = new Throwable(); 
						StackTraceElement[] elements = t.getStackTrace(); 
						profiler.startProfiling(elements, method, args, elapsed, sizes, rsizes, packageName);
				   return result;						
				}	
					
		   };            
		   Object proxyObject;
		   if(cp==null){			   
			   //System.out.println("no constructor arguments");
			   proxyObject = ProxyBuilder.forClass(TypeTest)
						.dexCache(context.getDir("dx", Context.MODE_PRIVATE))
						.handler(handler).build();
		   }else {		   
		       proxyObject = ProxyBuilder.forClass(TypeTest)
					.constructorArgValues(cp.getConstructorArgValues())
					.constructorArgTypes(cp.getConstructorArgTypes())
					.dexCache(context.getDir("dx", Context.MODE_PRIVATE))
					.handler(handler).build();
		   }		  
		   proxyObjectContainer.put(oid, proxyObject);
		   return (T)proxyObject;
		
		 } catch (Exception e) {
			e.printStackTrace();
			
			return null;
		}		
	
  }  // end of object creation


	static class ConnectTask extends AsyncTask<Socket,String,String> {
		int readcontrol = 0;
		@Override
		protected String doInBackground(Socket... socket) {
			try {
				Socket s = socket[0];
				InputStream  input = s.getInputStream();

				byte [] controlarray1 = new byte [4];

				boolean inputc=true;

				while(inputc){

					if(input.available()>0){

						readcontrol = input.read(controlarray1,0,controlarray1.length);

						inputc=false;
					}

					System.out.println("waiting from server!!!");
				}

			} catch (Exception e) {

				e.printStackTrace();
			}
			System.out.println("Control data is read : "+ readcontrol);

			return String.valueOf(readcontrol);


		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);

		}


	}


//public static byte[] getBytesFromBitmapNotCompressed(Bitmap bitmap){
//
//ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
//bitmap.copyPixelsToBuffer(byteBuffer);
//byte[] bytes = byteBuffer.array();
//return bytes;
//
//}



}// *****end of offloading Class *****






