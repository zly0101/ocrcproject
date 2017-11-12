package com.example.shinelon.ocrcamera.helper;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;
import com.example.shinelon.ocrcamera.SecondActivity;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Shinelon on 2017/6/30.
 */

public class AsycProcessTask extends AsyncTask<String,String,String> {

    private List<String> baiduRs;
    private List<String> tengxuRs;
    private ProgressDialog mProgressDialog;
    private final SecondActivity mSecondActivity;
    /**
     * str
     * 不能为null，不然会报错，因为子线程返回值必须为String
     */
    private static String str ="";
    private Retrofit retrofit;
    private RetorfitRequest request;

    public AsycProcessTask(SecondActivity activity){
        this.mSecondActivity = activity;
    }

    @Override
    public void onPreExecute(){
        mProgressDialog = new ProgressDialog(mSecondActivity);
        mProgressDialog.setMessage("准备识别");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
        setResult("");
        baiduRs = new ArrayList<>();
        tengxuRs = new ArrayList<>();
        retrofit = new Retrofit.Builder()
                .baseUrl("http://recognition.image.myqcloud.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        request = retrofit.create(RetorfitRequest.class);
    }
    @Override
    public String doInBackground(String... args){
        StringBuffer sb = new StringBuffer();
        String inputFile = args[0];
        // 通用文字识别参数设置
        GeneralBasicParams param = new GeneralBasicParams();
        param.setDetectDirection(true);
        File file = new File(inputFile);
        param.setImageFile(file);

        publishProgress("开始识别");

        /**
         * 此方法又开启了一个异步线程！并且是耗时的！！！
         */
    // 调用通用文字识别服务
    OCR.getInstance().recognizeGeneralBasic(param, new OnResultListener<GeneralResult>() {
        @Override
        public void onResult(GeneralResult result) {
            // 调用成功，返回GeneralResult对象
            for (WordSimple wordSimple : result.getWordList()) {
                // wordSimple不包含位置信息
                WordSimple word = wordSimple;
                String itemString = word.getWords().replaceAll("\\s*","").trim();
                baiduRs.add(itemString);
                Log.e("每个字段和字符数",itemString+"  "+itemString.length());
            }
            publishProgress("正在识别");
             if(baiduRs.size()>=1){
                 publishProgress("识别完成");
             }else{
                 publishProgress("识别失败");
             }
        }
        @Override
        public void onError(OCRError error) {
            // 调用失败，返回OCRError对象
            setResult(error.getMessage());
        }
    });
        RequestBody appid = RequestBody.create(null,"1253939683");
        RequestBody bucket = RequestBody.create(null,"hardblack");
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"),file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image",file.getName(),fileBody);
        String authorization = "";
        try {
            authorization = Authorization.generateKey();
            Log.e("权限",authorization);
        }catch (Exception e){
            e.printStackTrace();
        }

        Log.w("第二个识别",file.getAbsolutePath());
        try {
            request.getResult(authorization,appid,bucket,body)
                    .subscribe(new Observer<TentcentRs>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            Log.d("onSubscrie","first");
                        }
                        @Override
                        public void onNext(TentcentRs tentcentRs) {
                            Log.d("onNext","second");
                            List<TentcentRs.DataBean.ItemsBean> list = tentcentRs.getData().getItems();
                            Iterator iterator = list.iterator();
                            while (iterator.hasNext()){
                                TentcentRs.DataBean.ItemsBean itemsBean = (TentcentRs.DataBean.ItemsBean) iterator.next();
                                String itemString = itemsBean.getItemstring();
                                itemString = itemString.replaceAll("\\s*","").trim();
                                tengxuRs.add(itemString);
                                Log.w("腾讯每个字段及其字符数",itemString+"  "+itemString.length());
                            }

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("onError","three");
                        }

                        @Override
                        public void onComplete() {
                            Log.d("onComplete","four");
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
            Log.w("第二个识别:","出错了"+e.getMessage());
        }
        /**
         * 要有足够长的时间等待ocr线程的完成！
         */
        publishProgress("正在处理");
        try {
            Thread.sleep(500);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(!"".equals(getResult())){
            Log.e("getResult",getResult());
            return getResult();
        }else {
            compareListString(baiduRs,tengxuRs);
            Iterator<String> iterator = baiduRs.iterator();
            StringBuffer stringBuffer = new StringBuffer();
            while(iterator.hasNext()){
                stringBuffer.append(iterator.next()+"<br>");
            }
            String string = stringBuffer.toString();
            Log.e("stringbuffer",string);
            setResult("<html><body>"+string+"</body></html>");
            Log.e("getResult",getResult());
            Log.e("两者list字段各自长度",baiduRs.size()+"    "+tengxuRs.size());
            return getResult();
        }
    }


    @Override
    public void onProgressUpdate(String... values){
        mProgressDialog.setMessage(values[0]);
    }
    @Override
    public void onPostExecute(String result){
        Log.d("onPostExecute",result);
        if(result.length()>=1){
            mSecondActivity.updateResult(result);
        }else{
            mSecondActivity.updateResult("识别出错，请重试！");
        }
        if(mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
    }

    public void setResult(String result){
        str = result;
        Log.d("setResult()","我是setReult "+str);
    }

    public String getResult(){
        Log.d("getResult()","我是getReult "+str);
        return str;
    }

    public void compareListString(List<String> baiduRs,List<String> tengxuRs){
        for(int i=0;i<baiduRs.size()&&baiduRs.size()==tengxuRs.size();i++){
            String a =baiduRs.get(i);
            String b =tengxuRs.get(i);
            Log.e("长度",a.length()+"   "+b.length());
            String tempA = a.replaceAll("[\\p{Punct}\\p{Space}]+","");
            String tempB = b.replaceAll("[\\p{Punct}\\p{Space}]+","");
            if(tempA.equalsIgnoreCase(tempB)){
                Log.e("比较","相等");
            }else {
                Log.e("a与b",a+" 不等 "+b);
                baiduRs.set(i,"<font color=\"#ff0000\">" + a + "</font>");
            }
        }
    }


    }
