import api from './index';
// 複数のAPIを一つに統合して、ポイントとPayPayの残高を同時に取得する関数
export const fetchAllBalances = async (userId) => {
    console.log(`[API] ポイント・PayPay残高一括照会リクエスト (ユーザーID: ${userId})`);
    try {
        // バックエンドの新しいエンドポイントに合わせて修正: /api/balances/{userId}
        const response = await api.get(`/api/balances/${userId}`);
        return response.data; // { pointBalance, paypayBalance } オブジェクトを返します
    } catch (error) {
        if (error.response?.status === 404) {
            console.warn('[API] 残高情報なし。初期値0で処理。');
            return { pointBalance: 0, paypayBalance: 0 };
        }
        console.error('[API] 残高照会失敗:', error);
        throw error;
    }
};

// 既存の関数を新しい fetchAllBalances 関数を使用して再構成
export const fetchPointBalance = async (userId) => {
    const balances = await fetchAllBalances(userId);
    return balances.pointBalance;
};

export const fetchPayPayBalance = async (userId) => {
    const balances = await fetchAllBalances(userId);
    return balances.paypayBalance;
};

// PayPayでポイントチャージ
export const chargePointsWithPayPay = async (userId, amount) => {
    console.log(`[API] PayPayでポイントチャージ: 金額: ${amount} (ユーザーID: ${userId})`);
    const payload = { userId, amount };
    try {
        // バックエンドの新しいエンドポイントに合わせて修正
        const response = await api.post('/api/balances/charge/paypay', payload);
        return response.data;
    } catch (error) {
        console.error('[API] ポイントチャージ失敗:', error);
        throw error;
    }
};

// クレジットカードでポイントチャージ
export const chargePointsWithCreditCard = async (userId, amount) => {
    console.log(`[API] クレジットカードでポイントチャージ: 金額: ${amount} (ユーザーID: ${userId})`);
    const payload = { userId, amount };
    try {
        // バックエンドの新しいエンドポイントに合わせて修正
        const response = await api.post('/api/balances/charge/card', payload);
        return response.data;
    } catch (error) {
        console.error('[API] ポイントチャージ失敗:', error);
        throw error;
    }
};

// ポイントをPayPayに返金
export const refundPointsToPayPay = async (userId, amount) => {
    console.log(`[API] ポイントをPayPayに返金: 金額: ${amount} (ユーザーID: ${userId})`);
    const payload = { userId, amount };
    try {
        // バックエンドの新しいエンドポイントに合わせて修正
        const response = await api.post('/api/balances/refund/paypay', payload);
        return response.data;
    } catch (error) {
        console.error('[API] ポイント返金失敗:', error);
        throw error;
    }
};

// バックエンドAPI仕様に合わせて processVirtualPayment 関数を再構成
export const processVirtualPayment = async (userId, method, totalAmount) => {
    console.log(`[API] 仮想決済リクエスト: ${method}, 金額: ${totalAmount} (ユーザーID: ${userId})`);
    const payload = { userId: userId, amount: totalAmount };

    // バックエンドの新しいAPIに合わせたロジック
    // ここでは'PAYPAY_REFUND'の場合のみを例として提供
    if (method.toUpperCase() === 'PAYPAY_REFUND') {
        const response = await refundPointsToPayPay(userId, totalAmount);
        console.log(`[API] 仮想決済成功:`, response);
        return response;
    } else {
        console.error('[API] サポートされていない決済方法です:', method);
        throw new Error('サポートされていない決済方法です。');
    }
};

export const processVirtualPayment = async (userId, method, totalAmount) => {
    console.log(`[API] 仮想決済リクエスト: ${method}, 金額: ${totalAmount} (ユーザーID: ${userId})`);
    const payload = { userId: userId, amount: totalAmount };

    if (method.toUpperCase() === 'PAYPAY_REFUND') {
        const response = await refundPointsToPayPay(userId, totalAmount);
        console.log(`[API] 仮想決済成功:`, response);
        return response;
    } else if (method === 'point') {
        console.log(`[API] ポイント決済: バックエンドでのポイント消費は注文確定時に行われます。`);

        return { message: "ポイント決済処理はフロントエンドで完了しました。" };
    } else if (method === 'paypay') {
        console.log(`[API] PayPay仮想残高からの支払い: 金額: ${totalAmount} (ユーザーID: ${userId})`);
        const response = await api.post('/api/balances/deduct/paypay', payload);
        return response.data;
    } else if (method === 'virtual_credit_card') {
        console.log(`[API] クレジットカード仮想残高からの支払い: 金額: ${totalAmount} (ユーザーID: ${userId})`);
        const response = await api.post('/api/balances/deduct/card', payload);
        return response.data;
    }
    else {
        console.error('[API] サポートされていない決済方法です:', method);
        throw new Error('サポートされていない決済方法です。');
    }
};


export const topUpPointsWithPayPay = async (userId, amount) => {
    console.log(`[API] PayPayでポイントチャージリクエスト: ${amount}ポイント (ユーザーID: ${userId})`);
    const response = await api.post('/api/balances/charge/paypay', {
        userId,
        amount
    });
    return response.data;
};

