import api from './index';
export const fetchBalances = async (userId) => {
    console.log(`[API] 残高照会リクエスト (ユーザーID: ${userId})`);
        try {
            const paypayRes = await api.get('/api/balances/find', {
                params: { userId, paymentMethod: 'PAYPAY' }
            });
            const pointRes = await api.get('/api/balances/find', {
                params: { userId, paymentMethod: 'POINT' }
            });

            return {
                paypay: paypayRes.data.balance,
                point: pointRes.data.balance,
                virtualCreditCard: 0,
            };
        } catch (error) {
            if (error.response?.status === 404) {
                console.warn('[API]残高なし0円で処理 ');
                return {
                    paypay: 0,
                    point: 0,
                    virtualCreditCard: 0,
                };
            }
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
