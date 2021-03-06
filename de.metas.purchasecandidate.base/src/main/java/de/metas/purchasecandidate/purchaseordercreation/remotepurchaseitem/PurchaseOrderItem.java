package de.metas.purchasecandidate.purchaseordercreation.remotepurchaseitem;

import static org.adempiere.model.InterfaceWrapperHelper.load;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.annotation.Nullable;

import org.adempiere.bpartner.BPartnerId;
import org.adempiere.util.Check;
import org.adempiere.util.lang.ITableRecordReference;
import org.compiere.model.I_C_OrderLine;

import com.google.common.base.Objects;

import de.metas.purchasecandidate.PurchaseCandidate;
import de.metas.purchasecandidate.purchaseordercreation.remoteorder.NullVendorGatewayInvoker;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/*
 * #%L
 * de.metas.purchasecandidate.base
 * %%
 * Copyright (C) 2018 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

/**
 * Instances of this class represent a piece of a <b>factual</b> purchase order,
 * for which the system now needs to create a {@code C_Order} etc.
 *
 * @author metas-dev <dev@metasfresh.com>
 *
 */
@ToString(exclude = "purchaseCandidate") // exclude purchaseCandidate to avoid stacktrace, since purchaseCandidate can hold a reference to this
public class PurchaseOrderItem implements PurchaseItem
{
	public static PurchaseOrderItem cast(final PurchaseItem purchaseItem)
	{
		return (PurchaseOrderItem)purchaseItem;
	}

	@Getter
	private final int purchaseItemId;

	@Getter
	private final ITableRecordReference transactionReference;

	@Getter
	private final String remotePurchaseOrderId;

	@Getter(AccessLevel.PRIVATE)
	private final PurchaseCandidate purchaseCandidate;

	@Getter
	private final BigDecimal purchasedQty;

	@Getter
	private final LocalDateTime datePromised;

	@Getter
	private int purchaseOrderId;

	@Getter
	private int purchaseOrderLineId;

	@Builder(toBuilder = true)
	private PurchaseOrderItem(
			final int purchaseItemId,
			@NonNull final PurchaseCandidate purchaseCandidate,
			@NonNull final BigDecimal purchasedQty,
			@NonNull final LocalDateTime datePromised,
			@NonNull final String remotePurchaseOrderId,
			@Nullable final ITableRecordReference transactionReference,
			final int purchaseOrderId,
			final int purchaseOrderLineId)
	{
		this.purchaseItemId = purchaseItemId;

		this.purchaseCandidate = purchaseCandidate;

		this.purchasedQty = purchasedQty;
		this.datePromised = datePromised;
		this.remotePurchaseOrderId = remotePurchaseOrderId;

		this.purchaseOrderLineId = purchaseOrderLineId;
		this.purchaseOrderId = purchaseOrderId;

		final boolean remotePurchaseExists = !Objects.equal(remotePurchaseOrderId, NullVendorGatewayInvoker.NO_REMOTE_PURCHASE_ID);
		Check.errorIf(remotePurchaseExists && transactionReference == null,
				"If there is a remote purchase order, then the given transactionReference may not be null; remotePurchaseOrderId={}",
				remotePurchaseOrderId);
		this.transactionReference = transactionReference;
	}

	@Override
	public int getPurchaseCandidateId()
	{
		return getPurchaseCandidate().getPurchaseCandidateId();
	}

	public int getProductId()
	{
		return getPurchaseCandidate().getProductId();
	}

	public int getUomId()
	{
		return getPurchaseCandidate().getUomId();
	}

	public int getOrgId()
	{
		return getPurchaseCandidate().getOrgId();
	}

	public int getWarehouseId()
	{
		return getPurchaseCandidate().getWarehouseId();
	}

	public BPartnerId getVendorBPartnerId()
	{
		return getPurchaseCandidate().getVendorBPartnerId();
	}

	public LocalDateTime getDateRequired()
	{
		return getPurchaseCandidate().getDateRequired();
	}

	public int getSalesOrderId()
	{
		return getPurchaseCandidate().getSalesOrderId();
	}

	private BigDecimal getQtyToPurchase()
	{
		return getPurchaseCandidate().getQtyToPurchase();
	}

	public boolean purchaseMatchesRequiredQty()
	{
		return getPurchasedQty().compareTo(getQtyToPurchase()) == 0;
	}

	private boolean purchaseMatchesOrExceedsRequiredQty()
	{
		return getPurchasedQty().compareTo(getQtyToPurchase()) >= 0;
	}

	public void setPurchaseOrderLineIdAndMarkProcessed(final int purchaseOrderLineId)
	{
		this.purchaseOrderId = load(purchaseOrderLineId, I_C_OrderLine.class).getC_Order_ID();
		this.purchaseOrderLineId = purchaseOrderLineId;

		if (purchaseMatchesOrExceedsRequiredQty())
		{
			purchaseCandidate.markProcessed();
		}
	}
}
