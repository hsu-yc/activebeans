package org.activebeans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javassist.util.proxy.ProxyObject;

import org.activebeans.ConditionsMethodHandler.Order;

public class Table {

	private static final String FIRST_LIMIT = "limit 1";
	
	private String name;

	private List<Column> cols = new ArrayList<Column>();

	private String create;

	private String drop;

	private String alter;

	private String insert;
	
	private String select;
	
	private String selectAll;
	
	private String update;
	
	private String delete;
	
	private String deleteAll; 
	
	private String defaultOrder;
	
	private String reverseOrder;
	
	public Table(String name, List<Column> columns) {
		this.name = name;
		cols.addAll(columns);
		create = "create table if not exists " + name + "(";
		List<Column> keys = new ArrayList<Column>();
		for (int i = 0; i < cols.size(); i++) {
			Column c = cols.get(i);
			create += (i == 0 ? "" : ", ") + c.definition();
			if (c.key()) {
				keys.add(c);
			}
		}
		int numOfKeys = keys.size();
		for (int i = 0; i < numOfKeys; i++) {
			create += (i == 0 ? ", primary key(" : ", ")
					+ keys.get(i).name() + (i == numOfKeys - 1 ? ")" : "");
		}
		create += ")";
		drop = "drop table if exists " + name;
		alter = "alter table " + name;
		insert = "insert " + name + "(";
		for (int i=0;  i < cols.size(); i++) {
			Column c = cols.get(i);
			insert += (i == 0 ? "" : ", ") + c.name();
		}
		insert += ") values(";
		for (int i=0;  i < cols.size(); i++) {
			Column c = cols.get(i);
			insert += (i == 0 ? "" : ", ") + (c.autoIncrement()?"default":"?");
		}
		insert += ")";
		selectAll = "select";
		for (int i=0;  i < cols.size(); i++) {
			Column c = cols.get(i);
			selectAll += (i == 0 ? "" : ",") + " " + qualify(c.name());
		}
		selectAll += " from " + name;
		select = selectAll + " where";
		for (int i = 0; i < numOfKeys; i++) {
			select += (i == 0 ? "" : " and")
				+ " " + qualify(keys.get(i).name()) + " = ?";
		}
		update = "update " + name + " set";
		boolean firstUpdateCol = true;
		for (int i=0;  i < cols.size(); i++) {
			Column c = cols.get(i);
			if(!c.key()){
				update += (firstUpdateCol ? "" : ",") + " " + c.name() + " = ?";
				firstUpdateCol = false;
			}
		}
		update += " where";
		for (int i = 0; i < numOfKeys; i++) {
			update += (i == 0 ? "" : " and")
				+ " " + keys.get(i).name() + " = ?";
		}
		deleteAll = "delete from " + name;
		delete = deleteAll + " where";
		for (int i = 0; i < numOfKeys; i++) {
			delete += (i == 0 ? "" : " and")
				+ " " + keys.get(i).name() + " = ?";
		}
		defaultOrder = "order by";
		for (int i = 0; i < numOfKeys; i++) {
			defaultOrder += (i == 0 ? "" : ",") + " " + qualify(keys.get(i).name());
		}
		reverseOrder = "order by";
		for (int i = 0; i < numOfKeys; i++) {
			reverseOrder += (i == 0 ? "" : ",") + " " + qualify(keys.get(i).name()) + " desc";
		}
	}

	public Table(String name, Column... columns) {
		this(name, Arrays.asList(columns));
	}

	public String name() {
		return name;
	}

	public List<Column> columns() {
		return Collections.unmodifiableList(cols);
	}

	public String createStatment() {
		return create;
	}

	public String dropStatement() {
		return drop;
	}

	public String alterStatement(List<Column> cols) {
		String alterStmt = alter;
		for (int i = 0; i < cols.size(); i++) {
			alterStmt += (i == 0 ? "" : ",") + " add column "
				+ cols.get(i).definition();
		}
		return alterStmt;
	}

	public String alterStatement(Column... cols) {
		return alterStatement(Arrays.asList(cols));
	}
	
	public String insertStatement(){
		return insert;
	}
	
	public String selectStatement(){
		return select;
	}
	
	public String selectAllStatement(Object conds){
		ConditionsMethodHandler handler = (ConditionsMethodHandler) ((ProxyObject)conds).getHandler();
		return selectAll + handler.prepareWhereClause();
	}
	
	public String selectAllWithOrderStatement(){
		return selectAll + " " + defaultOrder;
	}
	
	public String selectAllWithOrderStatement(Object conds){
		ConditionsMethodHandler handler = (ConditionsMethodHandler) ((ProxyObject)conds).getHandler();
		return selectAllStatement(conds) + " " + propertyOrderClause(handler.orders(), defaultOrder);
	}
	
	public String selectAllWithReverseOrderStatement(){
		return selectAll + " " + reverseOrder;
	}
	
	public String selectAllWithReverseOrderStatement(Object conds){
		ConditionsMethodHandler handler = (ConditionsMethodHandler) ((ProxyObject)conds).getHandler();
		return selectAllStatement(conds) + " " + propertyOrderClause(handler.reverseOrders(), reverseOrder);
	}
	
	public String selectAllStatement(){
		return selectAll;
	}
	
	public String updateStatement(){
		return update;
	}
	
	public String deleteStatement(){
		return delete;
	}
	
	public String deleteAllStatement(){
		return deleteAll;
	}
	
	public String defaultOrder(){
		return defaultOrder;
	}
	
	public String firstLimit(){
		return FIRST_LIMIT;
	}

	public String selectFirstStatement() {
		return selectAll + " " + defaultOrder + " " + FIRST_LIMIT;
	}

	public String selectFirstStatement(Object conds) {
		return selectAllWithOrderStatement(conds) + " " + FIRST_LIMIT;
	}
	
	public String reverseOrder(){
		return reverseOrder;
	}

	public String selectLastStatement() {
		return selectAll + " " + reverseOrder + " " + FIRST_LIMIT;
	}

	public String selectLastStatement(Object conds) {
		return selectAllWithReverseOrderStatement(conds) + " " + FIRST_LIMIT;
	}
	
	public String updateAllStatement(Object options){
		String stmt = "update " + name;
		OptionsMethodHandler handler = (OptionsMethodHandler) ((ProxyObject)options).getHandler();
		boolean empty = true;
		for (Property prop : handler.properties().keySet()) {
			stmt += (empty?" set":",") + " " + ActiveBeansUtils.camelCaseToUnderscore(prop.name())
				+ " = ?";
			empty = false;
		}
		return stmt;
	}

	public String propertyOrderClause(Map<Property, Order> order, String defaultClause){
		String orderClause;
		if(order.isEmpty()){
			orderClause = defaultClause;
		}else{
			orderClause = propertyOrderClause(order);
		}
		return orderClause;
	}
	
	public String propertyOrderClause(Map<Property, Order> order){
		Map<String, Order> o = new LinkedHashMap<String, Order>(); 
		for (Entry<Property, Order> e : order.entrySet()) {
			o.put(qualify(ActiveBeansUtils.camelCaseToUnderscore(e.getKey().name())), e.getValue());
		}
		return stringOrderClause(o);
	}
		
	public static String stringOrderClause(Map<String, Order> order) {
		String clause = "";
		boolean first = true;
		for (Entry<String, Order> e : order.entrySet()) {
			clause += (first?"order by":",") + " "+ e.getKey() + " " + e.getValue();
			first = false;
		}
		return clause;
	}
	
	private String qualify(String identifier){
		return qualify(name, identifier);
	}
	
	public static String qualify(String qualifier, String identifier){
		return qualifier + "." + identifier;
	}

}
