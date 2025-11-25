<?php

return [
    /*
    |--------------------------------------------------------------------------
    | VNPAY Configuration
    |--------------------------------------------------------------------------
    |
    | Cấu hình cho VNPAY Payment Gateway
    |
    */

    'url' => env('VNPAY_URL', 'https://sandbox.vnpayment.vn/paymentv2/vpcpay.html'),

    'tmn_code' => env('VNPAY_TMN_CODE', ''),

    'hash_secret' => env('VNPAY_HASH_SECRET', ''),

    'return_url' => env('VNPAY_RETURN_URL', env('APP_URL', 'http://localhost:8000') . '/api/payment/vnpay/return'),

    'ipn_url' => env('VNPAY_IPN_URL', 'http://localhost:8000/api/payment/vnpay/ipn'),
];
