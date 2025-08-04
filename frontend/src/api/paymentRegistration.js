import api from './index';
// PayPay登録API
export const registerPayPay = async (userId, paypayId) => {
    // バックエンドの新しいエンドポイントに合わせて修正
    const response = await api.post('/api/register/paypay', {
        userId,
        paypayId
    });
    return response.data;
};

// カード登録API
export const registerCard = async (cardInfo) => {
    console.log(`[API] クレジットカード登録リクエスト (ユーザーID: ${cardInfo.userId})`);
    // このAPIは変更ありません
    const response = await api.post('/api/register/card', cardInfo);
    return response.data;
};
