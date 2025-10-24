<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class ChuyenBay extends Model
{
    protected $table = 'chuyen_bay';
    protected $fillable = [
        'ma_hang_hang_khong',
        'ma_may_bay',
        'ma_chuyen_bay',
        'ma_tuyen_bay',
        'gio_khoi_hanh',
        'gio_ha_canh',
        'tan_suat',
        'trang_thai'
    ];
    protected $casts = [
        'gio_khoi_hanh' => 'datetime',
        'gio_ha_canh' => 'datetime',
    ];

    public function hang_hang_khong()
    {
        return $this->belongsTo(HangHangKhong::class, 'ma_hang_hang_khong');
    }

    public function may_bay()
    {
        return $this->belongsTo(MayBay::class, 'ma_may_bay');
    }

    public function tuyen_bay()
    {
        return $this->belongsTo(TuyenBay::class, 'ma_tuyen_bay');
    }

    public function gia_ve()
    {
        return $this->hasMany(GiaVe::class, 'ma_chuyen_bay');
    }

    public function dat_ve()
    {
        return $this->hasMany(DatVe::class, 'ma_chuyen_bay');
    }
}
