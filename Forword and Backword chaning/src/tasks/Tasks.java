/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tasks;

/**
 *
 * @author ahmed
 */
public class Tasks {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        //resolution
        resolution pl = new resolution();
        System.out.println(pl.plResolution("((a OR (b AND c)) AND NOT b)", "a"));
        System.out.println(pl.plResolution("(a <=> (b AND C))", "b"));
        
        //forward 
        forward fc  = new forward("r", "p=>q;q=>r;p;q;");
        System.out.println(fc.execute());
        
        //backward
        backward bc  = new backward("p", "q=>p;r=>q;r;");
       // backward bc  = new backward("p", "p=>q;r=>q;r;");
        System.out.println(bc.execute());
    }
    
}
