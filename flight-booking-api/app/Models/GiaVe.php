<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class GiaVe extends Model
{
    protected $table = 'gia_ve';
    protected $fillable = [
        'ma_chuyen_bay',
        'hang_ve',
        'gia',
        'hanh_ly_ky_gui',
        'chinh_sach_huy_ve',
        'chinh_sach_doi_ve',
        'ngay_bat_dau',
        'ngay_ket_thuc'
    ];
    protected $casts = [
        'gia' => 'decimal:2',
        'ngay_bat_dau' => 'date',
        'ngay_ket_thuc' => 'date',
    ];

    public function chuyen_bay()
    {
        return $this->belongsTo(ChuyenBay::class, 'ma_chuyen_bay');
    }
}