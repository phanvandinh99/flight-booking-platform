<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class DatVe extends Model
{
    protected $table = 'dat_ve';
    protected $fillable = [
        'ma_khach_hang',
        'ma_chuyen_bay',
        'ma_dat_ve',
        'trang_thai',
        'thoi_gian_het_han_giu_cho',
        'tong_tien'
    ];
    protected $casts = [
        'thoi_gian_het_han_giu_cho' => 'datetime',
        'tong_tien' => 'decimal:2',
    ];

    public function khach_hang()
    {
        return $this->belongsTo(NguoiDung::class, 'ma_khach_hang');
    }

    public function chuyen_bay()
    {
        return $this->belongsTo(ChuyenBay::class, 'ma_chuyen_bay');
    }

    public function hanh_khach()
    {
        return $this->hasMany(HanhKhach::class, 'ma_dat_ve');
    }
}
