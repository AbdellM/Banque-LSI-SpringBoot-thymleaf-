package org.lsi.metier;

import java.util.Date;

import org.lsi.dao.CompteRepository;
import org.lsi.dao.EmployeRepository;
import org.lsi.dao.OperationRepository;
import org.lsi.entities.Compte;
import org.lsi.entities.Employe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.lsi.entities.Operation;
import org.lsi.entities.Retrait;
import org.lsi.entities.Versment;

@Service
public class OperationMetierImpl implements OperationMetier {
	
	@Autowired
	private OperationRepository operationRepository;
	@Autowired
	private CompteRepository compteRepository;
	@Autowired
	private EmployeRepository employeRepository;

	@Override
	@Transactional
	public boolean verser(String code, double montant, Long codeEmp) {
		Compte cp=compteRepository.findById(code).orElse(null);
		Employe e= employeRepository.findById(codeEmp).orElse(null);
		Operation o = new Versment();
		o.setDateOperation(new Date());
		o.setMontant(montant);
		o.setCompte(cp);
		o.setEmploye(e);
		operationRepository.save(o);
		cp.setSolde(cp.getSolde()+montant);
		return true;
	}

	@Override
	@Transactional
	public boolean retirer(String code, double montant, Long codeEmp) {
		Compte cp=compteRepository.findById(code).orElse(null);
		if(cp.getSolde()<montant) throw new RuntimeException("Solde Insuffisant");
		Employe e= employeRepository.findById(codeEmp).orElse(null);
		Operation o = new Retrait();
		o.setDateOperation(new Date());
		o.setMontant(montant);
		o.setCompte(cp);
		o.setEmploye(e);
		operationRepository.save(o);
		cp.setSolde(cp.getSolde()-montant);
		return true;
	}

	@Override
	@Transactional
	public boolean virement(String cpte1, String cpte2, double montant, Long codeEmp) {
		retirer(cpte1, montant, codeEmp);
		verser(cpte2, montant, codeEmp);
		return true;
	}

	@Override
	public PageOperation getOperation(String codeCompte, int page, int size) {
	
	Page<Operation> ops = operationRepository.getOperations(codeCompte, PageRequest.of(page, size,Sort.by(Sort.Direction.DESC,"dateOperation")));
	// 2 éme Solution
	//Compte cp = compteRepository.findById(codeCompte).orElse(null);
	//Page<Operation> ops2 = operationRepository.findByCompte(cp, (Pageable) new PageRequest(page, size));
	PageOperation pOp = new PageOperation();
			pOp.setOperations(ops.getContent());
			pOp.setNombreOperations(ops.getNumberOfElements());
			pOp.setPage(ops.getNumber());
			pOp.setTotalpages(ops.getTotalPages());
		return pOp;
	}
}
