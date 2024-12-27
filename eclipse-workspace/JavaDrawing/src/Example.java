

// 참고 스레드에서 보다 안전하게 사용할 수 있는 CopyOnWriteArrayList 간단한 설명

import java.util.concurrent.CopyOnWriteArrayList;

public class Example {
    public static void main(String[] args) throws InterruptedException { 
 
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();

        // 요소 추가
        list.add("Element1");
        list.add("Element2");


        // 스레드에서 보다 안전하게 사용할 수 있음
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                list.add("Element3");
            }
        });
        thread.start();
        
        // 요소 조회
        System.out.println(list.get(0)); // "Element1" 출력
        System.out.println(list.get(1)); // "Element2" 출력
        System.out.println(list.get(2)); // "Element3" 출력
        System.out.println();
        
        //for문 사용 요소 조회
        for (String item : list) {
            System.out.println(item);
        }
    }
}
