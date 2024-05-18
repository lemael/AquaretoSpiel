package service

import entity.*
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

// Dimension von Card und image card load
private const val DELFIN = "/images/gameSceneImages/tiere/delfin.png"
private const val DELFIN_BLITZ = "/images/gameSceneImages/tiere/delfin_blitz.png"
private const val DELFIN_BABY = "/images/gameSceneImages/tiere/delfin_baby.png"
private const val DELFIN_FISCH = "/images/gameSceneImages/tiere/delfin_fisch.png"
private const val DELFIN_M = "/images/gameSceneImages/tiere/delfin_m.png"
private const val DELFIN_W = "/images/gameSceneImages/tiere/delfin_w.png"
private const val EISBAER = "/images/gameSceneImages/tiere/eisbaer.png"
private const val EISBAER_BABY = "/images/gameSceneImages/tiere/eisbaer_baby.png"
private const val EISBAER_BLITZ = "/images/gameSceneImages/tiere/eisbaer_blitz.png"
private const val EISBAER_FISCH = "/images/gameSceneImages/tiere/eisbaer_fisch.png"
private const val EISBAER_M = "/images/gameSceneImages/tiere/eisbaer_m.png"
private const val EISBAER_W = "/images/gameSceneImages/tiere/eisbaer_w.png"
private const val FLUSSPFERD = "/images/gameSceneImages/tiere/flusspferd.png"
private const val FLUSSPFERD_BABY = "/images/gameSceneImages/tiere/flusspferd_baby.png"
private const val FLUSSPFERD_BLITZ = "/images/gameSceneImages/tiere/flusspferd_blitz.png"
private const val FLUSSPFERD_FISCH = "/images/gameSceneImages/tiere/flusspferd_fisch.png"
private const val FLUSSPFERD_M = "/images/gameSceneImages/tiere/flusspferd_m.png"
private const val FLUSSPFERD_W = "/images/gameSceneImages/tiere/flusspferd_w.png"
private const val KROKODIL = "/images/gameSceneImages/tiere/krokodil.png"
private const val KROKODIL_BABY = "/images/gameSceneImages/tiere/krokodil_baby.png"
private const val KROKODIL_BLITZ = "/images/gameSceneImages/tiere/krokodil_blitz.png"
private const val KROKODIL_FISCH = "/images/gameSceneImages/tiere/krokodil_fisch.png"
private const val KROKODIL_M = "/images/gameSceneImages/tiere/krokodil_m.png"
private const val KROKODIL_W = "/images/gameSceneImages/tiere/krokodil_w.png"
private const val PINGUIN = "/images/gameSceneImages/tiere/Pinguin.png"
private const val PINGUIN_BABY = "/images/gameSceneImages/tiere/pinguin_baby.png"
private const val PINGUIN_BLITZ = "/images/gameSceneImages/tiere/pinguin_blitz.png"
private const val PINGUIN_FISCH = "/images/gameSceneImages/tiere/pinguin_fisch.png"
private const val PINGUIN_M = "/images/gameSceneImages/tiere/pinguin_m.png"
private const val PINGUIN_W = "/images/gameSceneImages/tiere/pinguin_w.png"
private const val SCHILDKROETE = "/images/gameSceneImages/tiere/schildkroete.png"
private const val SCHILDKROETE_BABY = "/images/gameSceneImages/tiere/schildkroete_baby.png"
private const val SCHILDKROETE_BLITZ = "/images/gameSceneImages/tiere/schildkroete_blitz.png"
private const val SCHILDKROETE_FISCH = "/images/gameSceneImages/tiere/schildkroete_fisch.png"
private const val SCHILDKROETE_M = "/images/gameSceneImages/tiere/schildkroete_m.png"
private const val SCHILDKROETE_W = "/images/gameSceneImages/tiere/schildkroete_w.png"
private const val SEELOEWE = "/images/gameSceneImages/tiere/seeloewe.png"
private const val SEELOEWE_BABY = "/images/gameSceneImages/tiere/seeloewe_baby.png"
private const val SEELOEWE_BLITZ = "/images/gameSceneImages/tiere/seeloewe_blitz.png"
private const val SEELOEWE_FISCH = "/images/gameSceneImages/tiere/seeloewe_fisch.png"
private const val SEELOEWE_M = "/images/gameSceneImages/tiere/seeloewe_m.png"
private const val SEELOEWE_W = "/images/gameSceneImages/tiere/seeloewe_w.png"
private const val WAL = "/images/gameSceneImages/tiere/wal.png"
private const val WAL_BABY = "/images/gameSceneImages/tiere/wal_baby.png"
private const val WAL_BLITZ = "/images/gameSceneImages/tiere/wal_blitz.png"
private const val WAL_FISCH = "/images/gameSceneImages/tiere/wal_fisch.png"
private const val WAL_M = "/images/gameSceneImages/tiere/wal_m.png"
private const val WAL_W = "/images/gameSceneImages/tiere/wal_w.png"
private const val RUECKSEITE = "/images/gameSceneImages/tiere/rueckseite.png"
private const val WASSERPARK = "/images/gameSceneImages/wasserpark.png"
private const val AUSBAUTAFEL_GROSS = "/images/gameSceneImages/ausbautafel_gross.png"
private const val AUSBAUTAFEL_KLEIN = "/images/gameSceneImages/ausbautafel_klein.png"
private const val MUENZPLAETTCHEN = "/images/gameSceneImages/coin_card.png"

//private const val HOLZSCHEIBE = "/images/gameSceneImages/holzscheibe.png"
private const val TRANSPORTWAGON = "/images/gameSceneImages/transportwagen.png"
private const val COIN = "/images/gameSceneImages/coin.png"
private const val DEPOT = "/images/gameSceneImages/depot.png"
private const val MITARBEITER = "/images/gameSceneImages/mitarbeiter.png"
private const val EMPTY = "/images/gameSceneImages/tile.png"
private const val VOID = "/images/gameSceneImages/tile_dirt.png"
private const val IMG_HEIGHT = 200
private const val IMG_WIDTH = 130

class CardImageLoader {

    // main animals: default is trainable, never fish
    // region dolphin
    private val imageDelfin: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(DELFIN))
    private val imageDelfinM: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(DELFIN_M))
    private val imageDelfinW: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(DELFIN_W))
    private val imageDelfinBlitz: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(DELFIN_BLITZ))
    private val imageDelfinFisch: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(DELFIN_FISCH))
    private val imageDelfinBaby: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(DELFIN_BABY))
    // endregion
    // region orca
    private val imageWal: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(WAL))
    private val imageWalM: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(WAL_M))
    private val imageWalW: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(WAL_W))
    private val imageWalBlitz: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(WAL_BLITZ))
    private val imageWalFisch: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(WAL_FISCH))
    private val imageWalBaby: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(WAL_BABY))
    // endregion
    // region sealion
    private val imageSeeloewe: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(SEELOEWE))
    private val imageSeeloeweM: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(SEELOEWE_M))
    private val imageSeeloeweW: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(SEELOEWE_W))
    private val imageSeeloeweBlitz: BufferedImage =
        ImageIO.read(CardImageLoader::class.java.getResource(SEELOEWE_BLITZ))
    private val imageSeeloeweFisch: BufferedImage =
        ImageIO.read(CardImageLoader::class.java.getResource(SEELOEWE_FISCH))
    private val imageSeeloeweBaby: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(SEELOEWE_BABY))
    // endregion
    // side animals: default is not fish, never trainable
    // region hippo
    private val imageFlusspferde: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(FLUSSPFERD))
    private val imageFlusspferdeM: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(FLUSSPFERD_M))
    private val imageFlusspferdeW: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(FLUSSPFERD_W))
    private val imageFlusspferdeBlitz: BufferedImage =
        ImageIO.read(CardImageLoader::class.java.getResource(FLUSSPFERD_BLITZ))
    private val imageFlusspferdeFisch: BufferedImage =
        ImageIO.read(CardImageLoader::class.java.getResource(FLUSSPFERD_FISCH))
    private val imageFlusspferdeBaby: BufferedImage =
        ImageIO.read(CardImageLoader::class.java.getResource(FLUSSPFERD_BABY))
    // endregion
    // region turtle
    private val imageSchildkroete: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(SCHILDKROETE))
    private val imageSchildkroeteM: BufferedImage =
        ImageIO.read(CardImageLoader::class.java.getResource(SCHILDKROETE_M))
    private val imageSchildkroeteW: BufferedImage =
        ImageIO.read(CardImageLoader::class.java.getResource(SCHILDKROETE_W))
    private val imageSchildkroeteBlitz: BufferedImage =
        ImageIO.read(CardImageLoader::class.java.getResource(SCHILDKROETE_BLITZ))
    private val imageSchildkroeteFisch: BufferedImage =
        ImageIO.read(CardImageLoader::class.java.getResource(SCHILDKROETE_FISCH))
    private val imageSchildkroeteBaby: BufferedImage =
        ImageIO.read(CardImageLoader::class.java.getResource(SCHILDKROETE_BABY))
    // endregion
    // region crocodile
    private val imageKrokodil: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(KROKODIL))
    private val imageKrokodilM: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(KROKODIL_M))
    private val imageKrokodilW: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(KROKODIL_W))
    private val imageKrokodilBlitz: BufferedImage =
        ImageIO.read(CardImageLoader::class.java.getResource(KROKODIL_BLITZ))
    private val imageKrokodilFisch: BufferedImage =
        ImageIO.read(CardImageLoader::class.java.getResource(KROKODIL_FISCH))
    private val imageKrokodilBaby: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(KROKODIL_BABY))
    // endregion
    // region icebear
    private val imageEisbaer: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(EISBAER))
    private val imageEisbaerM: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(EISBAER_M))
    private val imageEisbaerW: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(EISBAER_W))
    private val imageEisbaerBlitz: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(EISBAER_BLITZ))
    private val imageEisbaerFisch: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(EISBAER_FISCH))
    private val imageEisbaerBaby: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(EISBAER_BABY))
    // endregion
    // region penguin
    private val imagePinguin: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(PINGUIN))
    private val imagePinguinM: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(PINGUIN_M))
    private val imagePinguinW: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(PINGUIN_W))
    private val imagePinguinBlitz: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(PINGUIN_BLITZ))
    private val imagePinguinFisch: BufferedImage =
        ImageIO.read(CardImageLoader::class.java.getResource(PINGUIN_FISCH))
    private val imagePinguinBaby: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(PINGUIN_BABY))
    // endregion

    // region other
    private val imageRueckseite: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(RUECKSEITE))
    private val imageWasserpark: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(WASSERPARK))
    private val imageCoin: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(COIN))
    private val imageAusbauTafelGross: BufferedImage =
        ImageIO.read(CardImageLoader::class.java.getResource(AUSBAUTAFEL_GROSS))
    private val imageAusbauTafelKlein: BufferedImage = ImageIO.read(
        CardImageLoader::class.java.getResource(
            AUSBAUTAFEL_KLEIN
        )
    )
    private val imageDepot: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(DEPOT))
    private val imageMuenzplaettchen: BufferedImage = ImageIO.read(
        CardImageLoader::class.java.getResource(
            MUENZPLAETTCHEN
        )
    )
    private val imageTransportWagon: BufferedImage = ImageIO.read(
        CardImageLoader::class.java.getResource(
            TRANSPORTWAGON
        )
    )
    private val imageMitarbeiter: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(MITARBEITER))
    private val imageEmpty: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(EMPTY))
    private val imageVoid: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(VOID))
    // private val imageHolzscheibe: BufferedImage = ImageIO.read(CardImageLoader::class.java.getResource(HOLZSCHEIBE))
    // endregion

    fun frontImageForTiere(tier: Depotable): BufferedImage {
        if(tier is Baby){
            return when(tier.type){
                AnimalType.DOLPHIN -> imageDelfinBaby
                AnimalType.ORCA -> imageWalBaby
                AnimalType.SEALION -> imageSeeloeweBaby
                AnimalType.HIPPO -> imageFlusspferdeBaby
                AnimalType.TURTLE -> imageSchildkroeteBaby
                AnimalType.CROCODILE -> imageKrokodilBaby
                AnimalType.ICEBEAR -> imageEisbaerBaby
                AnimalType.PENGUIN -> imagePinguinBaby
            }
        }
        require(tier is Animal)

        return frontImageForAnimal(tier)
    }

    fun frontImageWagonable(tier: Wagonable): BufferedImage {
        if (tier is CoinCard)
            return imageMuenzplaettchen

        require(tier is Animal)

        return frontImageForAnimal(tier)
    }
    fun imageForNullableWagonable(tier: Wagonable?): Visual {
        if(tier != null){
            if(tier is Empty)
                return ColorVisual(0, 0, 0, 0)
            else
                return ImageVisual(frontImageWagonable(tier))
        }
        else
            return ImageVisual(imageVoid)
    }

    private fun frontImageForAnimal(tier: Animal): BufferedImage{
        return when(tier.type){
            AnimalType.DOLPHIN -> { // dolphins default: trainable, not fish
                if(tier.gender == AnimalGender.MALE) imageDelfinM
                else if(tier.gender == AnimalGender.FEMALE) imageDelfinW
                else if(!tier.isTrainable) imageDelfinBlitz
                // should not be possible
                else if(tier.isFish) imageDelfinFisch
                else imageDelfin
            }
            AnimalType.ORCA -> { // orca default: trainable, not fish
                if(tier.gender == AnimalGender.MALE) imageWalM
                else if(tier.gender == AnimalGender.FEMALE) imageWalW
                else if(!tier.isTrainable) imageWalBlitz
                // should not be possible
                else if(tier.isFish) imageWalFisch
                else imageWal
            }
            AnimalType.SEALION -> { // sealion default: trainable, not fish
                if(tier.gender == AnimalGender.MALE) imageSeeloeweM
                else if(tier.gender == AnimalGender.FEMALE) imageSeeloeweW
                else if(!tier.isTrainable) imageSeeloeweBlitz
                // should not be possible
                else if(tier.isFish) imageSeeloeweFisch
                else imageSeeloewe
            }

            AnimalType.HIPPO -> { // hippo default: not fish, not trainable
                if(tier.gender == AnimalGender.MALE) imageFlusspferdeM
                else if(tier.gender == AnimalGender.FEMALE) imageFlusspferdeW
                else if(tier.isFish) imageFlusspferdeFisch
                // always not trainable
                //if(!tier.isTrainable) imageSeeloeweBlitz
                else imageFlusspferde
            }
            AnimalType.TURTLE -> { // hippo default: not fish, not trainable
                if(tier.gender == AnimalGender.MALE) imageSchildkroeteM
                else if(tier.gender == AnimalGender.FEMALE) imageSchildkroeteW
                else if(tier.isFish) imageSchildkroeteFisch
                // always not trainable
                //if(!tier.isTrainable) imageSchildkroeteBlitz
                else imageSchildkroete
            }
            AnimalType.CROCODILE -> { // hippo default: not fish, not trainable
                if(tier.gender == AnimalGender.MALE) imageKrokodilM
                else if(tier.gender == AnimalGender.FEMALE) imageKrokodilW
                else if(tier.isFish) imageKrokodilFisch
                // always not trainable
                //if(!tier.isTrainable) imageKrokodilBlitz
                else imageKrokodil
            }
            AnimalType.ICEBEAR -> { // hippo default: not fish, not trainable
                if(tier.gender == AnimalGender.MALE) imageEisbaerM
                else if(tier.gender == AnimalGender.FEMALE) imageEisbaerW
                else if(tier.isFish) imageEisbaerFisch
                // always not trainable
                //if(!tier.isTrainable) imageEisbaerBlitz
                else imageEisbaer
            }
            AnimalType.PENGUIN -> { // hippo default: not fish, not trainable
                if(tier.gender == AnimalGender.MALE) imagePinguinM
                else if(tier.gender == AnimalGender.FEMALE) imagePinguinW
                else if(tier.isFish) imagePinguinFisch
                // always not trainable
                //if(!tier.isTrainable) imagePinguinBlitz
                else imagePinguin
            }
        }
    }


    fun frontImageForCoin(player: Player): BufferedImage {
        if (player.numCoins == 1) {
            return imageCoin
        }
        return imageRueckseite
    }

    fun frontImageForTransportWagon(): BufferedImage {
        return imageTransportWagon
    }

    fun frontImageForMitarbeiter(): BufferedImage {
        return imageMitarbeiter
    }

    /**
    fun frontImageHolzscheibe(): BufferedImage {
    return imageHolzscheibe
    }
     */
    fun frontImageForGrossExtension(): BufferedImage {
        return imageAusbauTafelGross
    }

    fun frontImageForKleinExtension(): BufferedImage {
        return imageAusbauTafelKlein
    }

    fun frontImageForDepot(): BufferedImage {
        return imageDepot
    }

    fun frontImageWasserPark(): BufferedImage {
        return imageWasserpark
    }

    fun frontImageBackImage(): BufferedImage {
        return imageRueckseite
    }


}