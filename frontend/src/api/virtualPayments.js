import api from './index';
export const fetchBalances = async (userId) => {
    console.log(`[API] 残高照会リクエスト (ユーザーID: ${userId})`);
    try {
        // バックエンドの仕様に合わせて、paymentMethodごとに個別に取得
        const paypayRes = await api.get('/api/balances/find', { params: { userId, paymentMethod: 'PAYPAY' } });
        const pointRes = await api.get('/api/balances/find', { params: { userId, paymentMethod: 'POINT' } });

        const balances = {
            paypay: paypayRes.data.balance,
            point: pointRes.data.balance,
            // 仮想クレジットカードはバックエンドにないので固定値を維持
            virtualCreditCard: 20000,
        };
        console.log('[API] 残高照会成功:', balances);
        return balances;
    } catch (error) {
        console.error('[API] 残高照会失敗:', error);
        throw error;
    }
};

export const chargePoints = async (userId, method, amount) => {
    console.log(`[API] チャージリクエスト: ${method}, 金額: ${amount} (ユーザーID: ${userId})`);
    const paymentMethod = method.toUpperCase();
    const payload = { userId: userId, paymentMethod: paymentMethod, amount: amount };

    try {
        const response = await api.post('/api/balances/Charge', payload);
        console.log(`[API] チャージ成功:`, response.data);
        return response.data;
    } catch (error) {
        console.error('[API] チャージ失敗:', error);
        throw error;
    }
};

export const processVirtualPayment = async (userId, method, totalAmount) => {
    console.log(`[API] 仮想決済リクエスト: ${method}, 金額: ${totalAmount} (ユーザーID: ${userId})`);
    // バックエンドのPaymentMethod Enumに合わせるため大文字に変換
    const paymentMethod = method.toUpperCase();
    const payload = { userId: userId, paymentMethod: paymentMethod, amount: totalAmount };

    try {
        const response = await api.post('/api/balances/debit', payload);
        console.log(`[API] 仮想決済成功:`, response.data);
        return response.data;
    } catch (error) {
        console.error('[API] 仮想決済失敗:', error);
        throw error;
    }
};
