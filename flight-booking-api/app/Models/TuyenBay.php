<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class TuyenBay extends Model
{
    protected $table = 'tuyen_bay';
    protected $fillable = [
        'san_bay_di',
        'san_bay_den',
        'duoc_phe_duyet'
    ];

    public function san_bay_di()
    {
        return $this->belongsTo(SanBay::class, 'san_bay_di');
    }

    public function san_bay_den()
    {
        return $this->belongsTo(SanBay::class, 'san_bay_den');
    }

    public function chuyen_bay()
    {
        return $this->hasMany(ChuyenBay::class, 'ma_tuyen_bay');
    }
}
