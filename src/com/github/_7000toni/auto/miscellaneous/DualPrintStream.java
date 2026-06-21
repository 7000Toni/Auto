package com.github._7000toni.auto.miscellaneous;

import java.io.PrintStream;

public class DualPrintStream extends PrintStream {
    private final PrintStream p2;
    

    public DualPrintStream(PrintStream p1, PrintStream p2) {
        super(p1);
        this.p2 = p2;
    }
    
    @Override
    public void println() {
    	super.println();
    	p2.println();
    }
    
    @Override
    public void println(String s) {
    	super.println(s);
    	p2.println(s);
    }
    
    @Override
    public void println(boolean b) {
    	super.println(b);
    	p2.println(b);
    }
    
    @Override
    public void println(char c) {
    	super.println(c);
    	p2.println(c);
    }
    
    @Override
    public void println(char[] s) {
    	super.println(s);
    	p2.println(s);
    }
    
    @Override
    public void println(double d) {
    	super.println(d);
    	p2.println(d);
    }
    
    @Override
    public void println(float f) {
    	super.println(f);
    	p2.println(f);
    }
    
    @Override
    public void println(int i) {
    	super.println(i);
    	p2.println(i);
    }
    
    @Override
    public void println(long l) {
    	super.println(l);
    	p2.println(l);
    }
    
    @Override
    public void println(Object o) {
    	super.println(o);
    	p2.println(o);
    }
    
    @Override
    public PrintStream printf(String format, Object... args) {    	
    	p2.printf(format, args);
    	return super.printf(format, args);
    }
    
    @Override
    public void close() {
    	super.close();
    	p2.close();
    }
}
