/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import aima.core.logic.propositional.algorithms.KnowledgeBase;
import aima.core.logic.propositional.algorithms.LogicUtils;
import aima.core.logic.propositional.parsing.PEParser;
import aima.core.logic.propositional.parsing.ast.BinarySentence;
import aima.core.logic.propositional.parsing.ast.Sentence;
import aima.core.logic.propositional.parsing.ast.Symbol;
import aima.core.logic.propositional.parsing.ast.SymbolComparator;
import aima.core.logic.propositional.parsing.ast.UnarySentence;
import aima.core.logic.propositional.visitors.CNFClauseGatherer;
import aima.core.logic.propositional.visitors.CNFTransformer;
import aima.core.logic.propositional.visitors.SymbolClassifier;
import aima.core.util.Converter;
import aima.core.util.SetOps;
/**
 *
 * @author ahmed
 */
public class resolution {
    public boolean plResolution(String kbs, String alphaString) {
        KnowledgeBase kb = new KnowledgeBase();
        kb.tell(kbs);
        Sentence alpha = (Sentence) new PEParser().parse(alphaString);
        return plResolution(kb, alpha);
    }
    
     public boolean plResolution(KnowledgeBase kb, Sentence alpha) {
         
        //to add alpha to the eqution 
        Sentence kBAndNotAlpha = new BinarySentence("AND", kb.asSentence(),new UnarySentence(alpha));
        System.out.println(kBAndNotAlpha);
        
        //set the eqution in the CNF form   a list of sentence each sentance contain a specific var 
        Set<Sentence> clauses = new CNFClauseGatherer().getClausesFrom(new CNFTransformer().transform(kBAndNotAlpha));
        System.out.println(clauses);
        
        clauses = filterOutClausesWithTwoComplementaryLiterals(clauses);
        Set<Sentence> newClauses = new HashSet<Sentence>();
        
        while (true) {
            //compare each clause with all clauses
            List<List<Sentence>> pairs = getCombinationPairs(new Converter<Sentence>().setToList(clauses));
            
            for (int i = 0; i < pairs.size(); i++) {
                
                // compare each pare if one has not and other not have not then get thr result and print it
                List<Sentence> pair = pairs.get(i);
                Set<Sentence> resolvents = plResolve(pair.get(0), pair.get(1));
                resolvents = filterOutClausesWithTwoComplementaryLiterals(resolvents);
                
                System.out.println(pair.get(0) + " # " + pair.get(1) + " : " + resolvents);
                
                if (resolvents.contains(new Symbol("EMPTY_CLAUSE"))) {
                    return true;
                }
                newClauses = SetOps.union(newClauses, resolvents);
            }
            
            if (SetOps.intersection(newClauses, clauses).size() == newClauses.size()) {
                return false;
            }
            
            clauses = SetOps.union(newClauses, clauses);
            clauses = filterOutClausesWithTwoComplementaryLiterals(clauses);
        }

    }
     
     public Set<Sentence> plResolve(Sentence clause1, Sentence clause2) {
         
        Set<Sentence> resolvents = new HashSet<Sentence>();
        ClauseSymbols cs = new ClauseSymbols(clause1, clause2);
        Iterator<Symbol> iter = cs.getComplementedSymbols().iterator();
        
        while (iter.hasNext()) {
            Symbol symbol = iter.next();
            resolvents.add(createResolventClause(cs, symbol));
        }

        return resolvents;
    }
     
     private Set<Sentence> filterOutClausesWithTwoComplementaryLiterals(Set<Sentence> clauses) {
         
        Set<Sentence> filtered = new HashSet<Sentence>();
        SymbolClassifier classifier = new SymbolClassifier();
        Iterator<Sentence> iter = clauses.iterator();
        
        while (iter.hasNext()) {
            Sentence clause = iter.next();
            Set<Symbol> positiveSymbols = classifier.getPositiveSymbolsIn(clause);   // ==> get all postaive var in the sentence
            Set<Symbol> negativeSymbols = classifier.getNegativeSymbolsIn(clause);   // ==> get all negative var in the sentence
                    
            if ((SetOps.intersection(positiveSymbols, negativeSymbols).size() == 0)) {
                filtered.add(clause);  // seperate each clause and add it to the filtered
                //System.out.println(clause);
            }
        }
        return filtered;
    }
     
     private Sentence createResolventClause(ClauseSymbols cs, Symbol toRemove) {
        List<Symbol> positiveSymbols = new Converter<Symbol>().setToList(SetOps.union(cs.clause1PositiveSymbols, cs.clause2PositiveSymbols));
        List<Symbol> negativeSymbols = new Converter<Symbol>().setToList(SetOps.union(cs.clause1NegativeSymbols, cs.clause2NegativeSymbols));
        
        if (positiveSymbols.contains(toRemove)) {
            positiveSymbols.remove(toRemove);
        }
        
        if (negativeSymbols.contains(toRemove)) {
            negativeSymbols.remove(toRemove);
        }

        Collections.sort(positiveSymbols, new SymbolComparator());
        Collections.sort(negativeSymbols, new SymbolComparator());

        List<Sentence> sentences = new ArrayList<Sentence>();
        
        for (int i = 0; i < positiveSymbols.size(); i++) {
            sentences.add(positiveSymbols.get(i));
        }
        for (int i = 0; i < negativeSymbols.size(); i++) {
            sentences.add(new UnarySentence(negativeSymbols.get(i)));
        }
        if (sentences.size() == 0) {
            return new Symbol("EMPTY_CLAUSE");
        } else {
            return LogicUtils.chainWith("OR", sentences);
        }
    }
    private List<List<Sentence>> getCombinationPairs(List<Sentence> clausesList) {
        
        List<List<Sentence>> pairs = new ArrayList<List<Sentence>>();
        
        for (int i = 0; i < clausesList.size(); i++) {
            for (int j = i; j < clausesList.size(); j++) {
                List<Sentence> pair = new ArrayList<Sentence>();
                Sentence first = clausesList.get(i);
                Sentence second = clausesList.get(j);

                if (!(first.equals(second))) {
                    pair.add(first);
                    pair.add(second);
                    pairs.add(pair);
                }
            }
        }
        return pairs;
    }
    
    class ClauseSymbols {
        Set<Symbol> clause1Symbols, clause1PositiveSymbols,clause1NegativeSymbols;

        Set<Symbol> clause2Symbols, clause2PositiveSymbols,clause2NegativeSymbols;

        Set<Symbol> positiveInClause1NegativeInClause2,negativeInClause1PositiveInClause2;

        public ClauseSymbols(Sentence clause1, Sentence clause2) {

            SymbolClassifier classifier = new SymbolClassifier();

            clause1Symbols = classifier.getSymbolsIn(clause1);
            clause1PositiveSymbols = classifier.getPositiveSymbolsIn(clause1);
            clause1NegativeSymbols = classifier.getNegativeSymbolsIn(clause1);

            clause2Symbols = classifier.getSymbolsIn(clause2);
            clause2PositiveSymbols = classifier.getPositiveSymbolsIn(clause2);
            clause2NegativeSymbols = classifier.getNegativeSymbolsIn(clause2);

            positiveInClause1NegativeInClause2 = SetOps.intersection(clause1PositiveSymbols, clause2NegativeSymbols);
            negativeInClause1PositiveInClause2 = SetOps.intersection(clause1NegativeSymbols, clause2PositiveSymbols);

        }

        public Set<Symbol> getComplementedSymbols() {
            return SetOps.union(positiveInClause1NegativeInClause2,negativeInClause1PositiveInClause2);
        }

    }
}
