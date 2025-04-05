package um.tesoreria.mercadopago.service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import um.tesoreria.mercadopago.service.client.core.MercadoPagoContextClient;
import java.util.ArrayList;
import java.util.Arrays;

@Service
@Slf4j
public class CheckingService {

    private final PreferenceService preferenceService;
    private final MercadoPagoContextClient mercadoPagoContextClient;
    private final PaymentService paymentService;

    public CheckingService(PreferenceService preferenceService, MercadoPagoContextClient mercadoPagoContextClient, PaymentService paymentService) {
        this.preferenceService = preferenceService;
        this.mercadoPagoContextClient = mercadoPagoContextClient;
        this.paymentService = paymentService;
    }

    public String checkingCuota(Long chequeraCuotaId) {
        log.debug("Processing checkingCuota -> {}", chequeraCuotaId);
        preferenceService.createPreference(chequeraCuotaId);

        return "Checked";
    }

    public String checkingAllActive() {
        log.debug("Processing checkingAllActive");
        for (var chequeraCuotaId : mercadoPagoContextClient.findAllActiveChequeraCuota()) {
            log.debug(checkingCuota(chequeraCuotaId));
        }
        return "Checked";
    }

    public void checking_2024_11_12() {
        log.debug("Processing checking_2024_11_12");
        var idMercadoPagoIds = new ArrayList<>(Arrays.asList(
                95736016225L, 96079402070L, 96079422126L, 96075983216L, 96073967668L,
                95729559037L, 95728161643L, 96071030630L, 96071058126L, 95726912717L,
                95725688921L, 95724890775L, 96067763268L, 95720268939L, 96062834208L,
                96062128380L, 95717898011L, 96061567866L, 96061525400L, 96061331900L,
                95717806805L, 96061094088L, 95714842137L, 95714676717L, 96058017258L,
                96057919142L, 96057391990L, 96057463056L, 95713770747L, 95713687135L,
                96057129978L, 95712598165L, 95712171335L, 96054822086L, 96054343212L,
                96053297386L, 96051925936L, 96049324908L, 96049362172L, 95705372683L,
                96048092636L, 95703492997L, 96046344020L, 96046247576L, 95701723865L,
                95701081785L, 95699168667L, 96042629508L, 96042506046L, 96042442598L,
                95698874517L, 96042338372L, 96042152538L, 95698611421L, 95698552977L,
                96041851174L, 96039117414L, 95694480289L, 96037950670L, 96037684058L,
                95693720255L, 95693019877L, 96036397300L, 96036153354L, 96035207094L,
                96034810594L, 96034497862L, 96034467750L, 95691240937L, 96034275332L,
                96032324010L, 96032171618L, 95687459341L, 95685076449L, 96027870812L,
                96026950682L, 95683625097L, 96026737810L, 96026625664L, 96026621014L,
                96026426398L, 95682914613L, 95681942525L, 96024997202L, 95681406025L,
                96024482348L, 95680799497L, 96023650762L, 96023574272L, 96022958226L,
                96022876970L, 95678459511L, 96021705894L, 95676949803L, 95676282517L,
                95675552343L, 95673886069L, 95673887027L, 95673866467L, 96017250760L,
                95673556515L, 96015993648L, 95672940365L, 96015856350L, 96014159478L,
                96013251830L, 95669829117L, 96011365034L, 95668044933L, 96010195106L,
                96009938478L, 95666550289L, 96009429906L, 95665531435L, 95665517167L,
                96008271282L, 95664844131L, 96007343452L, 95663974233L, 96006669324L,
                96006373504L, 95662523539L, 95661426373L, 96004295062L, 96003934016L,
                95502463185L, 96002818190L, 95659447377L, 95658824517L, 96001908870L,
                96001641924L, 96001778824L, 96001614850L, 96001557018L, 95658155233L,
                95657993017L, 96001244378L, 95657740091L, 96000110608L, 95656892377L,
                95656862845L, 95656664729L, 95999707268L, 95999533272L, 95655734075L,
                95655670557L, 95998544372L, 95997798276L, 95997627920L, 95997208492L,
                95996912872L, 95996520114L, 95996480412L, 95652861869L, 95652633121L,
                95995139464L, 95994347864L, 95994209994L, 95994210580L, 95651127309L,
                95993565194L, 95648896151L, 95648762957L, 95991207862L, 95991079120L,
                95990834608L, 95990075804L, 95645967369L, 95988867750L, 95645469887L,
                95645568875L, 95988220992L, 95645178391L, 95644969531L, 95986887854L,
                95642701031L, 95983914780L, 95983197884L, 95983037170L, 95973924092L,
                95971606658L, 95970598828L
        ));

        for (Long idMercadoPago : idMercadoPagoIds) {
            log.debug("Checking idMercadoPago -> {}", idMercadoPago);
            paymentService.retrieveAndSavePayment(idMercadoPago.toString());
        }
    }
}
