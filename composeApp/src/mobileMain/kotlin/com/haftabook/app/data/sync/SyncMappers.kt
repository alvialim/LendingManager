package com.haftabook.app.data.sync

import com.haftabook.app.data.local.entity.CustomerEntity
import com.haftabook.app.data.local.entity.EmiEntity
import com.haftabook.app.data.local.entity.LoanEntity
import com.haftabook.app.data.remote.CustomerRemote
import com.haftabook.app.data.remote.EmiRemote
import com.haftabook.app.data.remote.LoanRemote

fun CustomerEntity.toCustomerRemote(): CustomerRemote {
    val rid = remoteId ?: error("customer.remoteId missing")
    return CustomerRemote(
        id = rid,
        name = name,
        mobile = mobile,
        loanType = loanType,
        createdDate = createdDate,
        updatedAt = updatedAt
    )
}

fun LoanEntity.toLoanRemote(customerRemoteId: String): LoanRemote {
    val rid = remoteId ?: error("loan.remoteId missing")
    return LoanRemote(
        id = rid,
        customerId = customerRemoteId,
        loanNumber = loanNumber,
        loanAmount = loanAmount,
        emiAmount = emiAmount,
        loanStartDate = loanStartDate,
        emiStartDate = emiStartDate,
        totalEmis = totalEmis,
        lastEmiDate = lastEmiDate,
        remainingAmount = remainingAmount,
        updatedAt = updatedAt
    )
}

fun EmiEntity.toEmiRemote(loanRemoteId: String): EmiRemote {
    val rid = remoteId ?: error("emi.remoteId missing")
    return EmiRemote(
        id = rid,
        loanId = loanRemoteId,
        emiNumber = emiNumber,
        emiAmount = emiAmount,
        emiDate = emiDate,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
