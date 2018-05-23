/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.idempiere.process;


import java.util.logging.Level;

import org.adempiere.base.Core;
import org.compiere.model.IProcessInfoParameter;
import org.compiere.product.IProductPricing;
import org.compiere.impl.MProject;
import org.compiere.impl.MProjectLine;
import org.compiere.product.MProduct;
import org.compiere.util.Msg;

import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;

/**
 *  Price Project Line.
 *
 *	@author Jorg Janke
 *	@version $Id: ProjectLinePricing.java,v 1.2 2006/07/30 00:51:02 jjanke Exp $
 */
public class ProjectLinePricing extends SvrProcess
{
	/**	Project Line from Record			*/
	private int 		m_C_ProjectLine_ID = 0;

	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		IProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else
				log.log(Level.SEVERE, "prepare - Unknown Parameter: " + name);
		}
		m_C_ProjectLine_ID = getRecord_ID();
	}	//	prepare

	/**
	 *  Perform process.
	 *  @return Message (clear text)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{
		if (m_C_ProjectLine_ID == 0)
			throw new IllegalArgumentException("No Project Line");
		MProjectLine projectLine = new MProjectLine (getCtx(), m_C_ProjectLine_ID, get_TrxName());
		if (log.isLoggable(Level.INFO)) log.info("doIt - " + projectLine);
		if (projectLine.getM_Product_ID() == 0)
			throw new IllegalArgumentException("No Product");
		//
		MProject project = new MProject (getCtx(), projectLine.getC_Project_ID(), get_TrxName());
		if (project.getM_PriceList_ID() == 0)
			throw new IllegalArgumentException("No PriceList");
		//
		boolean isSOTrx = true;
		IProductPricing pp = MProduct.getProductPricing();
		pp.setInitialValues(projectLine.getM_Product_ID(), project.getC_BPartner_ID(), 
				projectLine.getPlannedQty(), isSOTrx, get_TrxName());
		pp.setM_PriceList_ID(project.getM_PriceList_ID());
		pp.setPriceDate(project.getDateContract());
		//
		projectLine.setPlannedPrice(pp.getPriceStd());
		projectLine.setPlannedMarginAmt(pp.getPriceStd().subtract(pp.getPriceLimit()));
		projectLine.saveEx();
		//
		StringBuilder retValue = new StringBuilder().append(Msg.getElement(getCtx(), "PriceList")).append(pp.getPriceList()).append(" - ")
			.append(Msg.getElement(getCtx(), "PriceStd")).append(pp.getPriceStd()).append(" - ")
			.append(Msg.getElement(getCtx(), "PriceLimit")).append(pp.getPriceLimit());
		return retValue.toString();
	}	//	doIt

}	//	ProjectLinePricing
